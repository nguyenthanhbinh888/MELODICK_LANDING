package com.melodick.recs.controller;

import com.melodick.recs.dto.request.ArtistCreateUpdateRequest;
import com.melodick.recs.dto.request.ArtistReorderRequest;
import com.melodick.recs.dto.response.ApiResponse;
import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.service.ArtistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/artists")
public class AdminArtistController {

    private final ArtistService artistService;

    public AdminArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping
    public ResponseEntity<List<ArtistResponse>> getAllArtists(@RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(artistService.getAllArtists(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
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
}

