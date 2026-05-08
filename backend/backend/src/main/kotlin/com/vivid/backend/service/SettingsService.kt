package com.vivid.backend.service

import com.vivid.backend.domain.entity.internal.SettingsEntity
import com.vivid.backend.domain.entity.internal.SettingsRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SettingsService(
    private val repository: SettingsRepository,
) {

    @Cacheable("settings")
    @Transactional
    fun getSettings(): SettingsEntity {
        return repository.findSettingsEntity() ?: repository.save(SettingsEntity())
    }

    @CacheEvict("settings")
    @Transactional
    fun updateSettings(consumer: (SettingsEntity) -> Unit): SettingsEntity {
        val settingsEntity = repository.findSettingsEntity() ?: repository.save(SettingsEntity())
        consumer(settingsEntity)
        settingsEntity.lastUpdated = LocalDateTime.now()
        return repository.save(settingsEntity)
    }
}
