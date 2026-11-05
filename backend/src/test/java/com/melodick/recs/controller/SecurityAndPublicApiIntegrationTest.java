package com.melodick.recs.controller;

import com.melodick.recs.service.ArtistService;
import com.melodick.recs.service.DashboardService;
import com.melodick.recs.service.PublicService;
import com.melodick.recs.service.SiteSettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndPublicApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicService publicService;

    @MockBean
    private ArtistService artistService;

    @MockBean
    private SiteSettingService siteSettingService;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void publicSite_PermitAll_ReturnsOk() throws Exception {
        when(publicService.getPublicSiteSettings()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/public/site"))
                .andExpect(status().isOk());
    }

    @Test
    void publicArtists_PermitAll_ReturnsOk() throws Exception {
        when(publicService.getPublishedArtists()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/public/artists"))
                .andExpect(status().isOk());
    }

    @Test
    void adminArtists_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/artists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDashboard_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminArtists_WithAdminRole_ReturnsOk() throws Exception {
        when(artistService.getAllArtists(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/artists"))
                .andExpect(status().isOk());
    }
}

