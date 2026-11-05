package com.melodick.recs.service;

import com.melodick.recs.entity.SiteSetting;
import com.melodick.recs.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllSettingsAsMap() {
        List<SiteSetting> settings = siteSettingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (SiteSetting s : settings) {
            map.put(s.getSettingKey(), s.getSettingValue() != null ? s.getSettingValue() : "");
        }
        return map;
    }

    @Transactional
    public Map<String, String> updateSettings(Map<String, String> newSettings) {
        for (Map.Entry<String, String> entry : newSettings.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue() != null ? entry.getValue().trim() : "";

            siteSettingRepository.findBySettingKey(key).ifPresentOrElse(
                    setting -> {
                        setting.setSettingValue(value);
                        siteSettingRepository.save(setting);
                    },
                    () -> {
                        SiteSetting newSetting = new SiteSetting(key, value);
                        siteSettingRepository.save(newSetting);
                    }
            );
        }
        return getAllSettingsAsMap();
    }
}

