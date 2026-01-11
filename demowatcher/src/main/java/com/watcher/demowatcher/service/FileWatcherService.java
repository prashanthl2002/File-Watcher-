package com.watcher.demowatcher.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;
import com.watcher.demowatcher.model.Document;
import com.watcher.demowatcher.repository.DocumentRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileWatcherService {
	
	@Autowired
	private DocumentRepository documentRepository;
	
	@Autowired
    private  DocumentProcessorService documentProcessorService;
	
	  @Value("${app.upload.dir}")
	    private String watchDirectory;
	    
	    private WatchService watchService;
	    private Thread watcherThread;
	    private volatile boolean running = false;
	    
	    
	    @PostConstruct
	    public void init() {
	        log.info("Initializing File Watcher Service...");
	        startWatching();
	    }
	    
	    public void startWatching()
	    {
	    	try {
	            // Create WatchService
	            watchService = FileSystems.getDefault().newWatchService();
	            
	            // Get the path to watch
	            Path path = Paths.get(watchDirectory);
	            
	            // Create directory if it doesn't exist
	            if (!Files.exists(path)) {
	                Files.createDirectories(path);
	                log.info("Created watch directory: {}", path);
	            }
	            
	            // Register the directory with WatchService for specific events
	            path.register(
	                watchService,
	                StandardWatchEventKinds.ENTRY_CREATE,
	                StandardWatchEventKinds.ENTRY_MODIFY,
	                StandardWatchEventKinds.ENTRY_DELETE
	            );
	            
	            log.info("Started watching directory: {}", path.toAbsolutePath());
	            
	            running = true;
	            
	            // Start watcher thread
	            watcherThread = new Thread(() -> watchDirectory());
	            watcherThread.setName("FileWatcherThread");
	            watcherThread.setDaemon(true);
	            watcherThread.start();
	            
	            log.info("File Watcher Service started successfully!");
	            
	        } catch (IOException e) {
	            log.error("Failed to start File Watcher Service: {}", e.getMessage(), e);
	        }
	    }
	    
	    private void watchDirectory() {
	        log.info("Watcher thread started, monitoring for file changes...");
	        
	        while (running) {
	            try {
	                // Wait for events (blocking call)
	                WatchKey key = watchService.take();
	                
	                // Process all events
	                for (WatchEvent<?> event : key.pollEvents()) {
	                    WatchEvent.Kind<?> kind = event.kind();
	                    
	                    // Handle overflow
	                    if (kind == StandardWatchEventKinds.OVERFLOW) {
	                        log.warn("Event overflow occurred");
	                        continue;
	                    }
	                    
	                    // Get the filename
	                    @SuppressWarnings("unchecked")
	                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
	                    Path filename = ev.context();
	                    
	                    log.info("Event detected: {} - {}", kind.name(), filename);
	                    
	                    // Handle different event types
	                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
	                        handleFileCreated(filename);
	                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
	                        handleFileModified(filename);
	                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
	                        handleFileDeleted(filename);
	                    }
	                }
	                
	                // Reset the key - important!
	                boolean valid = key.reset();
	                if (!valid) {
	                    log.error("WatchKey is no longer valid, stopping watcher");
	                    break;
	                }
	                
	            } catch (InterruptedException e) {
	                log.info("Watcher thread interrupted, stopping...");
	                Thread.currentThread().interrupt();
	                break;
	            } catch (Exception e) {
	                log.error("Error in watcher thread: {}", e.getMessage(), e);
	            }
	        }
	        
	        log.info("Watcher thread stopped");
	    }
	    
	    
	    private void handleFileCreated(Path filename) {
	        log.info("New file detected: {}", filename);
	        
	        try {
	            // Small delay to ensure file is fully written
	            Thread.sleep(500);
	            
	            // Get full file path
	            Path fullPath = Paths.get(watchDirectory, filename.toString());
	            
	            // Check if file still exists and is readable
	            if (!Files.exists(fullPath) || !Files.isReadable(fullPath)) {
	                log.warn("File not readable or doesn't exist: {}", filename);
	                return;
	            }
	            
	            // Get file info
	            String originalName = filename.toString();
	            long fileSize = Files.size(fullPath);
	            FileType fileType = FileType.fromFileName(originalName);
	            
	            log.info("Processing new file: {} (Type: {}, Size: {} bytes)", 
	                     originalName, fileType, fileSize);
	            
	            // Create document record
	            Document document = new Document();
	            document.setFileName(originalName);
	            document.setOriginalName(originalName);
	            document.setFileType(fileType);
	            document.setFileSize(fileSize);
	            document.setFilePath(fullPath.toString());
	            document.setProcessingStatus(ProcessingStatus.PENDING);
	            
	            // Save to database
	            Document savedDocument = documentRepository.save(document);
	            log.info("Document record created with ID: {}", savedDocument.getId());
	            
	            // Process the file asynchronously
	            documentProcessorService.processDocument(savedDocument.getId());
	            
	        } catch (Exception e) {
	            log.error("Error handling file creation for {}: {}", filename, e.getMessage(), e);
	        }
	    }
	    
	    private void handleFileModified(Path filename) {
	        log.debug("File modified: {}", filename);
	        // Can implement logic to reprocess modified files if needed
	    }
	    
	    private void handleFileDeleted(Path filename) {
	        log.info("File deleted: {}", filename);
	        // Can implement logic to update database when files are manually deleted
	    }
	    
	    @PreDestroy
	    public void stopWatching() {
	        log.info("Stopping File Watcher Service...");
	        running = false;
	        
	        if (watcherThread != null) {
	            watcherThread.interrupt();
	        }
	        
	        if (watchService != null) {
	            try {
	                watchService.close();
	                log.info("WatchService closed successfully");
	            } catch (IOException e) {
	                log.error("Error closing WatchService: {}", e.getMessage(), e);
	            }
	        }
	        
	        log.info("File Watcher Service stopped");
	    }
	    
	    public boolean isRunning() {
	        return running;
	    }
	

}
