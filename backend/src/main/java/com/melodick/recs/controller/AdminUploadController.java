package com.melodick.recs.controller;

import com.melodick.recs.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final FileStorageService fileStorageService;

    public AdminUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileStorageService.storeArtistImage(file);
        return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
    }
}

