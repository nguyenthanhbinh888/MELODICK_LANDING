package com.melodick.recs.service;

import com.melodick.recs.dto.request.ArtistCreateUpdateRequest;
import com.melodick.recs.dto.request.ArtistLinkDto;
import com.melodick.recs.dto.request.ArtistReorderRequest;
import com.melodick.recs.dto.response.ArtistLinkResponse;
import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.entity.Artist;
import com.melodick.recs.entity.ArtistLink;
import com.melodick.recs.exception.ResourceNotFoundException;
import com.melodick.recs.repository.ArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> getAllArtists(String search) {
        List<Artist> artists;
        if (StringUtils.hasText(search)) {
            artists = artistRepository.searchByNameWithLinks(search.trim());
        } else {
            artists = artistRepository.findAllWithLinks();
        }
        return artists.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + id));
        return mapToResponse(artist);
    }

    @Transactional
    public ArtistResponse createArtist(ArtistCreateUpdateRequest request) {
        Artist artist = new Artist();
        artist.setName(request.getName().trim());
        artist.setSlug(StringUtils.hasText(request.getSlug()) ? request.getSlug().trim() : generateSlug(request.getName()));
        artist.setImageUrl(StringUtils.hasText(request.getImageUrl()) ? request.getImageUrl().trim() : null);
        artist.setBio(request.getBio());
        artist.setPublished(request.isPublished());
        artist.setDisplayOrder(request.getDisplayOrder());

        if (request.getLinks() != null) {
            for (ArtistLinkDto linkDto : request.getLinks()) {
                ArtistLink link = new ArtistLink(
                        linkDto.getPlatform().trim(),
                        linkDto.getUrl() != null ? linkDto.getUrl().trim() : "",
                        linkDto.getDisplayOrder()
                );
                artist.addLink(link);
            }
        }

        Artist saved = artistRepository.save(artist);
        return mapToResponse(saved);
    }

    @Transactional
    public ArtistResponse updateArtist(Long id, ArtistCreateUpdateRequest request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + id));

        artist.setName(request.getName().trim());
        if (StringUtils.hasText(request.getSlug())) {
            artist.setSlug(request.getSlug().trim());
        } else if (!StringUtils.hasText(artist.getSlug())) {
            artist.setSlug(generateSlug(request.getName()));
        }
        artist.setImageUrl(StringUtils.hasText(request.getImageUrl()) ? request.getImageUrl().trim() : null);
        artist.setBio(request.getBio());
        artist.setPublished(request.isPublished());
        artist.setDisplayOrder(request.getDisplayOrder());

        artist.clearLinks();
        if (request.getLinks() != null) {
            for (ArtistLinkDto linkDto : request.getLinks()) {
                ArtistLink link = new ArtistLink(
                        linkDto.getPlatform().trim(),
                        linkDto.getUrl() != null ? linkDto.getUrl().trim() : "",
                        linkDto.getDisplayOrder()
                );
                artist.addLink(link);
            }
        }

        Artist saved = artistRepository.save(artist);
        return mapToResponse(saved);
    }

    @Transactional
    public ArtistResponse toggleVisibility(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + id));
        artist.setPublished(!artist.isPublished());
        Artist saved = artistRepository.save(artist);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteArtist(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nghệ sĩ với ID: " + id));
        artistRepository.delete(artist);
    }

    @Transactional
    public void reorderArtists(ArtistReorderRequest request) {
        if (request.getOrders() != null) {
            for (ArtistReorderRequest.OrderItem item : request.getOrders()) {
                artistRepository.findById(item.getId()).ifPresent(artist -> {
                    artist.setDisplayOrder(item.getDisplayOrder());
                    artistRepository.save(artist);
                });
            }
        }
    }

    public ArtistResponse mapToResponse(Artist artist) {
        ArtistResponse res = new ArtistResponse();
        res.setId(artist.getId());
        res.setName(artist.getName());
        res.setSlug(artist.getSlug());
        res.setImageUrl(artist.getImageUrl());
        res.setBio(artist.getBio());
        res.setPublished(artist.isPublished());
        res.setDisplayOrder(artist.getDisplayOrder());
        res.setCreatedAt(artist.getCreatedAt());
        res.setUpdatedAt(artist.getUpdatedAt());

        if (artist.getLinks() != null) {
            List<ArtistLinkResponse> links = artist.getLinks().stream()
                    .map(l -> new ArtistLinkResponse(l.getId(), l.getPlatform(), l.getUrl(), l.getDisplayOrder()))
                    .collect(Collectors.toList());
            res.setLinks(links);
        }
        return res;
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}
