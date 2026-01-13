package com.watcher.demowatcher.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.watcher.demowatcher.model.Document;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Service
@Slf4j
public class ImageProcessorService {
		
	 @Value("${app.processed.dir}")
	    private String processedDir;

	    public String processImage(Document document) {
	        try {
	            File imageFile = Paths.get(document.getFilePath()).toFile();
	            
	            // Read the image
	            BufferedImage image = ImageIO.read(imageFile);
	            
	            if (image == null) {
	                throw new RuntimeException("Unable to read image file");
	            }
	            
	            // Get image properties
	            int width = image.getWidth();
	            int height = image.getHeight();
	            String format = getImageFormat(document.getFileName());
	            
	            log.info("Image properties - Width: {}, Height: {}, Format: {}", width, height, format);
	            
	            // Generate thumbnail
	            String thumbnailPath = generateThumbnail(imageFile, document.getFileName());
	            
	            // Calculate aspect ratio
	            double aspectRatio = (double) width / height;
	            
	            String metadata = String.format(
	                "Image processed successfully. Width: %d, Height: %d, Format: %s, Aspect Ratio: %.2f, Thumbnail: %s",
	                width, height, format, aspectRatio, thumbnailPath
	            );
	            
	            log.info("Image processing complete: {}x{} {}", width, height, format);
	            
	            return metadata;
	            
	        } catch (Exception e) {
	            log.error("Error processing image: {}", e.getMessage(), e);
	            throw new RuntimeException("Image processing failed: " + e.getMessage(), e);
	        }
	    }
	    
	    private String generateThumbnail(File originalImage, String originalFileName) {
	        try {
	            // Create thumbnails directory
	            Path thumbnailDir = Paths.get(processedDir, "thumbnails");
	            if (!Files.exists(thumbnailDir)) {
	                Files.createDirectories(thumbnailDir);
	            }
	            
	            // Generate thumbnail filename
	            String thumbnailFileName = "thumb_" + originalFileName;
	            Path thumbnailPath = thumbnailDir.resolve(thumbnailFileName);
	            
	            // Create thumbnail (200x200 max size, maintain aspect ratio)
	            Thumbnails.of(originalImage)
	                    .size(200, 200)
	                    .keepAspectRatio(true)
	                    .toFile(thumbnailPath.toFile());
	            
	            log.info("Thumbnail created: {}", thumbnailPath);
	            
	            return thumbnailPath.toString();
	            
	        } catch (IOException e) {
	            log.error("Error creating thumbnail: {}", e.getMessage(), e);
	            return "Thumbnail creation failed";
	        }
	    }
	    
	    private String getImageFormat(String fileName) {
	        int lastDot = fileName.lastIndexOf('.');
	        if (lastDot == -1) {
	            return "UNKNOWN";
	        }
	        return fileName.substring(lastDot + 1).toUpperCase();
	    }

}
