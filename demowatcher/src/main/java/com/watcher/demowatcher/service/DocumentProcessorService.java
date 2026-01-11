package com.watcher.demowatcher.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.watcher.demowatcher.constants.ProcessingStatus;
import com.watcher.demowatcher.model.Document;
import com.watcher.demowatcher.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessorService {

	@Autowired
	private PdfProcessorService pdfProcessorService;

	@Autowired
	private DocumentRepository documentRepository;

	@Value("${app.processed.dir}")
	private String processedDir;

	@Value("${app.failed.dir}")
	private String failedDir;

	@Async
	public void processDocument(Long documentId) {
		log.info("Starting to process document ID: {}", documentId);

		Document document = documentRepository.findById(documentId)
				.orElseThrow(() -> new RuntimeException("Document not found"));

		try {

			document.setProcessingStatus(ProcessingStatus.PROCESSING);
			documentRepository.save(document);

			String metadata = null;
			switch (document.getFileType()) {
			case PDF:
				log.info("Processing PDF file: {}", document.getFileName());
				metadata = pdfProcessorService.processPdf(document);
				break;
			}

			document.setProcessingStatus(ProcessingStatus.COMPLETED);
			document.setProcessedAt(LocalDateTime.now());
			document.setMetadata(metadata);
			documentRepository.save(document);

			moveToProcessed(document);

			log.info("Successfully processed document ID: {}", documentId);
		} catch (Exception e) {
			log.error("Error processing document ID {}: {}", documentId, e.getMessage(), e);

			document.setProcessingStatus(ProcessingStatus.FAILED);
			document.setErrorMessage(e.getMessage());
			document.setProcessedAt(LocalDateTime.now());
			documentRepository.save(document);

			moveToFailed(document);
		}
	}

	private void moveToProcessed(Document document) {
		try {
			Path sourcePath = Paths.get(document.getFilePath());
			Path targetPath = Paths.get(processedDir, document.getFileName());

			// Create directory if not exists
			Files.createDirectories(targetPath.getParent());

			// Move file
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			// Update file path in database
			document.setFilePath(targetPath.toString());
			documentRepository.save(document);

			log.info("Moved file to processed folder: {}", targetPath);

		} catch (IOException e) {
			log.error("Error moving file to processed folder: {}", e.getMessage(), e);
		}
	}

	private void moveToFailed(Document document) {
		try {
			Path sourcePath = Paths.get(document.getFilePath());
			Path targetPath = Paths.get(failedDir, document.getFileName());

			// Create directory if not exists
			Files.createDirectories(targetPath.getParent());

			// Move file
			Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

			// Update file path in database
			document.setFilePath(targetPath.toString());
			documentRepository.save(document);

			log.info("Moved file to failed folder: {}", targetPath);

		} catch (IOException e) {
			log.error("Error moving file to failed folder: {}", e.getMessage(), e);
		}
	}

}
