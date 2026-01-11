package com.watcher.demowatcher.service;

import java.io.File;
import java.nio.file.Paths;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.watcher.demowatcher.model.Document;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfProcessorService {
	
	public String processPdf(Document document)
	{
		try {
			File pdfFile =Paths.get(document.getFilePath()).toFile();
			PDDocument pdDocument=Loader.loadPDF(pdfFile);
			
			int pageCount=pdDocument.getNumberOfPages();
			
			 PDFTextStripper stripper =new PDFTextStripper();
	            String text = stripper.getText(pdDocument);
	            
	            int wordCount = text.split("\\s+").length;
	            int charCount = text.length();
	            
	            pdDocument.close();
	            
	            String metadata = String.format("PDF processed successfully. Pages: %d, Words: %d, Characters: %d", 
	                                           pageCount, wordCount, charCount);
	            
	            log.info("PDF processing complete: {} pages, {} words", pageCount, wordCount);
	            
	            // You can also save the extracted text to a file or database if needed
	            
	            return metadata;
		}
		catch (Exception e) {
			  log.error("Error processing PDF: {}", e.getMessage(), e);
	            throw new RuntimeException("PDF processing failed: " + e.getMessage(), e);
		}
	}
}
