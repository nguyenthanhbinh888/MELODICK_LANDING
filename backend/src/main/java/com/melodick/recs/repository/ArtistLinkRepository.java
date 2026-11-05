package com.melodick.recs.repository;

import com.melodick.recs.entity.ArtistLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistLinkRepository extends JpaRepository<ArtistLink, Long> {
    List<ArtistLink> findByArtistIdOrderByDisplayOrderAsc(Long artistId);
}
