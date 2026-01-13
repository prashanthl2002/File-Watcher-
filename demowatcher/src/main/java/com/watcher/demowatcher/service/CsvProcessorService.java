package com.watcher.demowatcher.service;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import com.watcher.demowatcher.model.Document;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CsvProcessorService {
	
	 public String processCsv(Document document) {
	        try {
	            Reader reader = new FileReader(Paths.get(document.getFilePath()).toFile());
	            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
	            
	            int recordCount = 0;
	            int columnCount = csvParser.getHeaderMap().size();
	            
	            log.info("CSV Headers: {}", csvParser.getHeaderNames());
	            
	            for (CSVRecord record : csvParser) {
	                recordCount++;
	                // Here you can process each record
	                // For now, we just count them
	                log.debug("Processing record {}: {}", recordCount, record.toMap());
	            }
	            
	            csvParser.close();
	            reader.close();
	            
	            String metadata = String.format("CSV processed successfully. Rows: %d, Columns: %d, Headers: %s", 
	                                           recordCount, columnCount, csvParser.getHeaderNames());
	            
	            log.info("CSV processing complete: {} rows, {} columns", recordCount, columnCount);
	            
	            return metadata;
	            
	        } catch (Exception e) {
	            log.error("Error processing CSV: {}", e.getMessage(), e);
	            throw new RuntimeException("CSV processing failed: " + e.getMessage(), e);
	        }
	    }
}
