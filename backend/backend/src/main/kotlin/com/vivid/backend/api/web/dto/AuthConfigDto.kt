package com.vivid.backend.api.web.dto

data class AuthConfigDto(
    val issuer: String?,
    val logoutUrl: String?
)
