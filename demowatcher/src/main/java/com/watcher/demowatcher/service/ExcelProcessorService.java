package com.watcher.demowatcher.service;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.watcher.demowatcher.model.Document;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ExcelProcessorService {
	
	 public String processExcel(Document document) {
	        try {
	            FileInputStream fis = new FileInputStream(Paths.get(document.getFilePath()).toFile());
	            
	            // Determine if it's .xls or .xlsx
	            Workbook workbook;
	            String fileName = document.getFileName().toLowerCase();
	            
	            if (fileName.endsWith(".xlsx")) {
	                workbook = new XSSFWorkbook(fis);
	                log.info("Processing XLSX file");
	            } else if (fileName.endsWith(".xls")) {
	                workbook = new HSSFWorkbook(fis);
	                log.info("Processing XLS file");
	            } else {
	                throw new RuntimeException("Unsupported Excel format");
	            }
	            
	            // Get sheet count
	            int sheetCount = workbook.getNumberOfSheets();
	            int totalRows = 0;
	            int totalColumns = 0;
	            List<String> sheetNames = new ArrayList<>();
	            
	            // Process each sheet
	            for (int i = 0; i < sheetCount; i++) {
	                Sheet sheet = workbook.getSheetAt(i);
	                String sheetName = sheet.getSheetName();
	                sheetNames.add(sheetName);
	                
	                int rowCount = sheet.getPhysicalNumberOfRows();
	                totalRows += rowCount;
	                
	                // Get column count from first row (header)
	                if (rowCount > 0) {
	                    Row firstRow = sheet.getRow(sheet.getFirstRowNum());
	                    if (firstRow != null) {
	                        int colCount = firstRow.getPhysicalNumberOfCells();
	                        if (colCount > totalColumns) {
	                            totalColumns = colCount;
	                        }
	                        
	                        // Log header information
	                        log.info("Sheet: {}, Rows: {}, Columns: {}", sheetName, rowCount, colCount);
	                        
	                        // Extract headers from first row
	                        List<String> headers = new ArrayList<>();
	                        for (Cell cell : firstRow) {
	                            headers.add(getCellValueAsString(cell));
	                        }
	                        log.debug("Headers in {}: {}", sheetName, headers);
	                    }
	                }
	            }
	            
	            workbook.close();
	            fis.close();
	            
	            String metadata = String.format(
	                "Excel processed successfully. Sheets: %d, Total Rows: %d, Max Columns: %d, Sheet Names: %s",
	                sheetCount, totalRows, totalColumns, sheetNames
	            );
	            
	            log.info("Excel processing complete: {} sheets, {} total rows", sheetCount, totalRows);
	            
	            return metadata;
	            
	        } catch (Exception e) {
	            log.error("Error processing Excel: {}", e.getMessage(), e);
	            throw new RuntimeException("Excel processing failed: " + e.getMessage(), e);
	        }
	    }
	    
	    private String getCellValueAsString(Cell cell) {
	        if (cell == null) {
	            return "";
	        }
	        
	        switch (cell.getCellType()) {
	            case STRING:
	                return cell.getStringCellValue();
	            case NUMERIC:
	                if (DateUtil.isCellDateFormatted(cell)) {
	                    return cell.getDateCellValue().toString();
	                } else {
	                    return String.valueOf(cell.getNumericCellValue());
	                }
	            case BOOLEAN:
	                return String.valueOf(cell.getBooleanCellValue());
	            case FORMULA:
	                return cell.getCellFormula();
	            case BLANK:
	                return "";
	            default:
	                return "";
	        }
	    }
	
}
