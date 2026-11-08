package com.melodick.recs.service;

import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.dto.response.ArtistStatsResponse;
import com.melodick.recs.dto.response.TopVideoResponse;
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
    private final PlatformStatsService platformStatsService;

    public PublicService(SiteSettingService siteSettingService,
                         ArtistRepository artistRepository,
                         ArtistService artistService,
                         PlatformStatsService platformStatsService) {
        this.siteSettingService = siteSettingService;
        this.artistRepository = artistRepository;
        this.artistService = artistService;
        this.platformStatsService = platformStatsService;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPublicSiteSettings() {
        return siteSettingService.getAllSettingsAsMap();
    }

    @Transactional
    public List<ArtistResponse> getPublishedArtists() {
        List<Artist> artists = artistRepository.findPublishedArtistsWithLinks();
        Map<Long, ArtistStatsResponse> statsMap = platformStatsService.getStatsForArtists(artists);

        return artists.stream().map(artist -> {
            ArtistResponse res = artistService.mapToResponse(artist);
            res.setStats(statsMap.get(artist.getId()));
            return res;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ArtistStatsResponse getArtistStats(Long artistId, boolean forceRefresh) {
        return platformStatsService.getArtistStats(artistId, forceRefresh);
    }

    @Transactional(readOnly = true)
    public TopVideoResponse getArtistTopVideo(Long artistId) {
        return platformStatsService.getTopVideo(artistId);
    }
}
