package com.melodick.recs.controller;

import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.service.PublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;

    public PublicController(PublicService publicService) {
        this.publicService = publicService;
    }

    @GetMapping("/site")
    public ResponseEntity<Map<String, String>> getSiteSettings() {
        return ResponseEntity.ok(publicService.getPublicSiteSettings());
    }

    @GetMapping("/artists")
    public ResponseEntity<List<ArtistResponse>> getPublishedArtists() {
        return ResponseEntity.ok(publicService.getPublishedArtists());
    }
}

