package com.melodick.recs.controller;

import com.melodick.recs.dto.request.ArtistCreateUpdateRequest;
import com.melodick.recs.dto.request.ArtistReorderRequest;
import com.melodick.recs.dto.response.ApiResponse;
import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.dto.response.ArtistStatsResponse;
import com.melodick.recs.service.ArtistService;
import com.melodick.recs.service.PlatformStatsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/artists")
public class AdminArtistController {

    private final ArtistService artistService;
    private final PlatformStatsService platformStatsService;

    public AdminArtistController(ArtistService artistService, PlatformStatsService platformStatsService) {
        this.artistService = artistService;
        this.platformStatsService = platformStatsService;
    }

    @GetMapping
    public ResponseEntity<List<ArtistResponse>> getAllArtists(@RequestParam(value = "search", required = false) String search) {
        List<ArtistResponse> responses = artistService.getAllArtists(search);
        List<Long> ids = responses.stream().map(ArtistResponse::getId).collect(Collectors.toList());
        Map<Long, ArtistStatsResponse> cachedStats = platformStatsService.getCachedStatsForArtistIds(ids);

        for (ArtistResponse res : responses) {
            res.setStats(cachedStats.get(res.getId()));
        }
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getArtistById(@PathVariable Long id) {
        ArtistResponse res = artistService.getArtistById(id);
        res.setStats(platformStatsService.getArtistStats(id, false));
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<ArtistResponse> createArtist(@Valid @RequestBody ArtistCreateUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artistService.createArtist(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtistResponse> updateArtist(@PathVariable Long id,
                                                       @Valid @RequestBody ArtistCreateUpdateRequest request) {
        return ResponseEntity.ok(artistService.updateArtist(id, request));
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ArtistResponse> toggleVisibility(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.toggleVisibility(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa nghệ sĩ thành công"));
    }

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderArtists(@Valid @RequestBody ArtistReorderRequest request) {
        artistService.reorderArtists(request);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật thứ tự hiển thị thành công"));
    }

    @PostMapping("/{id}/refresh-stats")
    public ResponseEntity<ArtistStatsResponse> refreshStats(@PathVariable Long id) {
        return ResponseEntity.ok(platformStatsService.forceRefreshArtistStats(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ArtistStatsResponse> getStats(@PathVariable Long id) {
        return ResponseEntity.ok(platformStatsService.getArtistStats(id, false));
    }
}
