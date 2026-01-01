package org.testspringboot.unibuy.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.testspringboot.unibuy.common.Result;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("File is empty");
        }
        
        // Ensure directory exists
        // Use absolute path to avoid permission issues or relative path confusion
        File dir = new File(new File(uploadPath).getAbsolutePath());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
             suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + suffix;

        try {
            File dest = new File(dir, newFilename);
            file.transferTo(dest);
            // Return URL (assuming static mapping is configured to /files/**)
            // Add full URL prefix for easier debugging on frontend
            return Result.success("http://localhost:8080/files/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("Upload failed: " + e.getMessage());
        }
    }
}
