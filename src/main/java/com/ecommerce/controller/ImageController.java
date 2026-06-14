package com.ecommerce.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/uploads/" + filename);
        
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] image = Files.readAllBytes(file.toPath());
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(image);
    }
}