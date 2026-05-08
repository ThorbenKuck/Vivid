package com.vivid.backend.api.client

import com.vivid.backend.domain.support.ApplicationIdentifier
import com.vivid.backend.service.EnvironmentService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class ClientIdentificationResolver(
    private val properties: ClientProperties,
    private val environmentService: EnvironmentService,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == ApplicationIdentifier::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java) ?: return null
        val id = request.getHeader(properties.applicationNameHeader) ?: return null
        val environmentId = request.getEnvironmentId() ?: return null
        val environment = environmentService.findEnvironment(environmentId) ?: return null
        val token = request.getHeader(properties.clientTokenHeader)

        return ApplicationIdentifier(id, environment, token)
    }

    private fun HttpServletRequest.getEnvironmentId(): String? {
        val header = getHeader(properties.environmentHeader)
        if (header != null) {
            return header
        }
        val attributes = getAttribute("org.springframework.web.servlet.View.pathVariables")
        if (attributes == null || attributes !is Map<*, *>) {
            return null
        }
        val attribute = attributes["environment"] ?: attributes["environmentId"]
        if (attribute == null || attribute !is String) {
            return null
        }

        return attribute
    }
}