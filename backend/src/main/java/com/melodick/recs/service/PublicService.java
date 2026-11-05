package com.melodick.recs.service;

import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.entity.Artist;
import com.melodick.recs.repository.ArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PublicService {

    private final SiteSettingService siteSettingService;
    private final ArtistRepository artistRepository;
    private final ArtistService artistService;

    public PublicService(SiteSettingService siteSettingService,
                         ArtistRepository artistRepository,
                         ArtistService artistService) {
        this.siteSettingService = siteSettingService;
        this.artistRepository = artistRepository;
        this.artistService = artistService;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPublicSiteSettings() {
        return siteSettingService.getAllSettingsAsMap();
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> getPublishedArtists() {
        List<Artist> artists = artistRepository.findPublishedArtistsWithLinks();
        return artists.stream().map(artistService::mapToResponse).collect(Collectors.toList());
    }
}
