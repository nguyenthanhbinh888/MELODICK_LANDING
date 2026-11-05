package com.melodick.recs.service;

import com.melodick.recs.dto.response.DashboardStatsResponse;
import com.melodick.recs.repository.ArtistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final ArtistRepository artistRepository;

    public DashboardService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long total = artistRepository.count();
        long published = artistRepository.countByPublished(true);
        long hidden = artistRepository.countByPublished(false);

        return new DashboardStatsResponse(total, published, hidden);
    }
}

