package com.vivid.backend.api.config

import org.springframework.stereotype.Service

@Service
class ApiTokenService(
    private val properties: ApiSecurityProperties,
) {
    fun isValid(token: () -> String?): Boolean {
        if (properties.token == null) {
            return true
        }

        return token() == properties.token
    }
}