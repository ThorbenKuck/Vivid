package com.vivid.backend.api.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class ApiTokenAuthenticationFilter(
    private val apiTokenService: ApiTokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!apiTokenService.isValid { request.getHeader("X-API-TOKEN") }) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API token")
            return
        }

        val authentication = UsernamePasswordAuthenticationToken(
            "api-client",
            null,
            listOf(SimpleGrantedAuthority("ROLE_API_CLIENT"))
        )

        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }
}