package com.melodick.recs.repository;

import com.melodick.recs.entity.ArtistPlatformStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistPlatformStatsRepository extends JpaRepository<ArtistPlatformStats, Long> {

    @Query("SELECT s FROM ArtistPlatformStats s JOIN FETCH s.artist WHERE s.artist.id = :artistId")
    Optional<ArtistPlatformStats> findByArtistId(@Param("artistId") Long artistId);

    @Query("SELECT s FROM ArtistPlatformStats s JOIN FETCH s.artist WHERE s.artist.id IN :artistIds")
    List<ArtistPlatformStats> findAllByArtistIdIn(@Param("artistIds") Collection<Long> artistIds);

    void deleteByArtistId(Long artistId);
}
