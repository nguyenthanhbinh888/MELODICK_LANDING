package com.melodick.recs.controller;

import com.melodick.recs.service.SiteSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/site-settings")
public class AdminSiteSettingController {

    private final SiteSettingService siteSettingService;

    public AdminSiteSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(siteSettingService.getAllSettingsAsMap());
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, String> settings) {
        return ResponseEntity.ok(siteSettingService.updateSettings(settings));
    }
}
