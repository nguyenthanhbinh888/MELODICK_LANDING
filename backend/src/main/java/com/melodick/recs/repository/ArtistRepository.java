package com.melodick.recs.repository;

import com.melodick.recs.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    @Query("SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.links WHERE a.published = true ORDER BY a.displayOrder ASC")
    List<Artist> findPublishedArtistsWithLinks();

    @Query("SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.links ORDER BY a.displayOrder ASC")
    List<Artist> findAllWithLinks();

    @Query("SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.links WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY a.displayOrder ASC")
    List<Artist> searchByNameWithLinks(@Param("search") String search);

    long countByPublished(boolean published);
}
