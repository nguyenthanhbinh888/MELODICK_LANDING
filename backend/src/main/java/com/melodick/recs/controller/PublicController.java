package com.melodick.recs.controller;

import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.dto.response.ArtistStatsResponse;
import com.melodick.recs.dto.response.TopVideoResponse;
import com.melodick.recs.service.PublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/artists/{id}/stats")
    public ResponseEntity<ArtistStatsResponse> getArtistStats(
            @PathVariable Long id,
            @RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
        return ResponseEntity.ok(publicService.getArtistStats(id, refresh));
    }

    @GetMapping("/artists/{id}/top-video")
    public ResponseEntity<TopVideoResponse> getArtistTopVideo(@PathVariable Long id) {
        return ResponseEntity.ok(publicService.getArtistTopVideo(id));
    }
}
