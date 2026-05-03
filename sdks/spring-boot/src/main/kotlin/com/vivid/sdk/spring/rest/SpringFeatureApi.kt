package com.vivid.sdk.spring.rest

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.api.Feature
import com.vivid.sdk.spring.VividProperties
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

private val logger = LoggerFactory.getLogger(SpringFeatureApi::class.java)

/**
 * [FeatureApi] implementation that uses Spring's [RestClient] to fetch feature states from the Vivid backend.
 */
class SpringFeatureApi(
    private val restClient: RestClient,
    private val restProperties: VividRestProperties,
    private val vividProperties: VividProperties
) : FeatureApi {

    override fun fetchFeature(key: String): Feature? {
        if (key.isBlank()) {
            return null
        }

        try {
            logger.trace("Attempting to fetch feature \"$key\"")
            return restClient.get()
                .uri {
                    it.path("/api/client/features/{environment}/{id}")
                        .build(vividProperties.environment.trim().removeSuffix("/"), key)
                }
                .headers {
                    it.accept = listOf(MediaType.APPLICATION_JSON)
                    it.add(restProperties.applicationIdHeaderName, vividProperties.applicationId)
                    restProperties.apiToken?.let { token -> it.add(restProperties.apiTokenHeaderName, token) }
                }
                .retrieve()
                .toEntity<Feature>()
                .body
                .also {
                    logger.trace("Fetched feature \"$key\" as \"${it?.id}\"")
                }
        } catch (e: HttpClientErrorException.NotFound) {
            logger.warn("Feature $key not found", e)
        } catch (e: Exception) {
            logger.warn("Error fetching feature $key: ${e.message}")
        }
        return null
    }

    override fun fetchAllFeatures(): List<Feature>? {
        try {
            logger.debug("Fetching all features for environment ${vividProperties.environment}")
            return restClient.get()
                .uri {
                    it.path("/api/client/features/{environment}") // Nutze .path() statt Segmente für mehr Kontrolle
                        .build(vividProperties.environment.trim().removeSuffix("/"))                }
                .headers {
                    it.accept = listOf(MediaType.APPLICATION_JSON)
                    it.add(restProperties.applicationIdHeaderName, vividProperties.applicationId)
                    restProperties.apiToken?.let { token -> it.add(restProperties.apiTokenHeaderName, token) }
                }
                .retrieve()
                .toEntity<List<Feature>>()
                .body
        } catch (e: Exception) {
            logger.warn("Error fetching all features for environment ${vividProperties.environment}: ${e.message}")
            return null
        }
    }
}
