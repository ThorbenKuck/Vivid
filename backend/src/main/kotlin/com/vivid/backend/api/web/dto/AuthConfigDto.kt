package com.vivid.backend.api.web.dto

data class AuthConfigDto(
    val issuerName: String?,
    val issuer: String?,
    val logoutUrl: String?
)
