package com.watcher.demowatcher.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;
import com.watcher.demowatcher.dto.DocumentResponseDTO;
import com.watcher.demowatcher.dto.UploadResponseDTO;
import com.watcher.demowatcher.service.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
	
	@Autowired
	private DocumentService documentService;
	
	 @PostMapping("/upload")
	    public ResponseEntity<UploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
	        UploadResponseDTO response = documentService.uploadFile(file);
	        return new ResponseEntity<>(response, HttpStatus.CREATED);
	    }
	    
	    @GetMapping
	    public ResponseEntity<List<DocumentResponseDTO>> getAllDocuments() {
	        List<DocumentResponseDTO> documents = documentService.getAllDocuments();
	        return ResponseEntity.ok(documents);
	    }
	    
	    @GetMapping("/{id}")
	    public ResponseEntity<DocumentResponseDTO> getDocumentById(@PathVariable Long id) {
	        DocumentResponseDTO document = documentService.getDocumentById(id);
	        return ResponseEntity.ok(document);
	    }
	    
	    @GetMapping("/status/{status}")
	    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByStatus(@PathVariable ProcessingStatus status) {
	        List<DocumentResponseDTO> documents = documentService.getDocumentsByStatus(status);
	        return ResponseEntity.ok(documents);
	    }
	    
	    @GetMapping("/type/{fileType}")
	    public ResponseEntity<List<DocumentResponseDTO>> getDocumentsByType(@PathVariable FileType fileType) {
	        List<DocumentResponseDTO> documents = documentService.getDocumentsByType(fileType);
	        return ResponseEntity.ok(documents);
	    }
	    
	    @DeleteMapping("/{id}")
	    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
	        documentService.deleteDocument(id);
	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Document deleted successfully");
	        return ResponseEntity.ok(response);
	    }
	    
	    @GetMapping("/health")
	    public ResponseEntity<Map<String, String>> healthCheck() {
	        Map<String, String> response = new HashMap<>();
	        response.put("status", "UP");
	        response.put("message", "Document Processing System is running");
	        return ResponseEntity.ok(response);
	    }
}
