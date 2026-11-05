package com.melodick.recs.service;

import com.melodick.recs.dto.request.ArtistCreateUpdateRequest;
import com.melodick.recs.dto.request.ArtistLinkDto;
import com.melodick.recs.dto.response.ArtistResponse;
import com.melodick.recs.entity.Artist;
import com.melodick.recs.entity.ArtistLink;
import com.melodick.recs.exception.ResourceNotFoundException;
import com.melodick.recs.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @InjectMocks
    private ArtistService artistService;

    private Artist sampleArtist;

    @BeforeEach
    void setUp() {
        sampleArtist = new Artist();
        sampleArtist.setId(1L);
        sampleArtist.setName("MIRAGEMFXNK");
        sampleArtist.setSlug("miragemfxnk");
        sampleArtist.setPublished(true);
        sampleArtist.setDisplayOrder(1);

        ArtistLink link = new ArtistLink("Spotify", "https://spotify.com", 1);
        link.setId(10L);
        sampleArtist.addLink(link);
    }

    @Test
    void getArtistById_Found_ReturnsArtistResponse() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(sampleArtist));

        ArtistResponse res = artistService.getArtistById(1L);

        assertNotNull(res);
        assertEquals("MIRAGEMFXNK", res.getName());
        assertEquals(1, res.getLinks().size());
    }

    @Test
    void getArtistById_NotFound_ThrowsResourceNotFoundException() {
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> artistService.getArtistById(999L));
    }

    @Test
    void createArtist_Success() {
        ArtistCreateUpdateRequest req = new ArtistCreateUpdateRequest();
        req.setName("NEW_ARTIST");
        req.setDisplayOrder(4);
        req.setPublished(true);

        List<ArtistLinkDto> links = new ArrayList<>();
        links.add(new ArtistLinkDto("Spotify", "https://spotify.com/new", 1));
        req.setLinks(links);

        when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> {
            Artist a = invocation.getArgument(0);
            a.setId(4L);
            return a;
        });

        ArtistResponse response = artistService.createArtist(req);

        assertNotNull(response);
        assertEquals("NEW_ARTIST", response.getName());
        assertEquals(4L, response.getId());
        assertEquals(1, response.getLinks().size());
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void toggleVisibility_Success() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(sampleArtist));
        when(artistRepository.save(any(Artist.class))).thenReturn(sampleArtist);

        ArtistResponse response = artistService.toggleVisibility(1L);

        assertFalse(sampleArtist.isPublished());
        verify(artistRepository).save(sampleArtist);
    }
}

