package com.melodick.recs.service;

import com.melodick.recs.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.init();
    }

    @Test
    void storeArtistImage_ValidImage_ReturnsUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        String url = fileStorageService.storeArtistImage(file);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/artists/"));
        assertTrue(url.endsWith(".png"));
    }

    @Test
    void storeArtistImage_EmptyFile_ThrowsBadRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[]{}
        );

        assertThrows(BadRequestException.class, () -> fileStorageService.storeArtistImage(emptyFile));
    }

    @Test
    void storeArtistImage_InvalidMimeType_ThrowsBadRequestException() {
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThrows(BadRequestException.class, () -> fileStorageService.storeArtistImage(textFile));
    }
}

