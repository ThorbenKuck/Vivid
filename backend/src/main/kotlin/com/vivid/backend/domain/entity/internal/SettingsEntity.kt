package com.vivid.backend.domain.entity.internal

import com.vivid.backend.domain.entity.BaseUuidEntity
import com.vivid.backend.domain.support.DurationConverter
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Duration
import java.time.LocalDateTime

@Entity(name = "Settings")
@Table(name = "vivid_settings")
class SettingsEntity(
    /**
     * Whether client tokens are required for client registration.
     *
     * If true, clients must provide a unique token when registering.
     * If false, clients can register without a token but must have unique names.
     *
     * This setting can be used to enforce the uniqueness of clients when having conflicting names.
     *
     * By default, we assume that each client has a unique name.
     * Because of this, client tokens are not required for registration.
     * If your applications can have conflicting names, consider enabling client tokens to ensure uniqueness.
     */
    var requireClientTokens: Boolean = false,
    /**
     * Whether clients are dynamically registered when first seen.
     *
     * This setting can be used to control the registration process of clients.
     * When enabled, clients are automatically registered when first seen.
     * When disabled, clients must be manually registered before they can be used.
     *
     * By default, dynamic client registration is enabled.
     * This means that every application you deploy is automatically registered with vivid.
     */
    var allowDynamicClientRegistration: Boolean = true,
    /**
     * How long a client is considered online before it is considered offline.
     */
    @Convert(converter = DurationConverter::class)
    var onlineThreshold: Duration = Duration.ofMinutes(7),
    /**
     * Whether this setting should be used or not.
     *
     * Can be helpfull when multiple settings are maintained in parallel.
     */
    val active: Boolean = true,
    /**
     * A flag when these settings where last updated.
     */
    var lastUpdated: LocalDateTime = LocalDateTime.now(),
): BaseUuidEntity()
