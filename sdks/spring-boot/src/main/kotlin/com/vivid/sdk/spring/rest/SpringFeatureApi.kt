package com.vivid.sdk.spring.rest

import com.vivid.sdk.FeatureApi
import com.vivid.sdk.api.Feature
import com.vivid.sdk.spring.VividProperties
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

private val logger = LoggerFactory.getLogger(SpringFeatureApi::class.java)

class SpringFeatureApi(
    private val restClient: RestClient,
    private val restProperties: VividRestProperties,
    private val vividProperties: VividProperties
) : FeatureApi {

    override fun fetchFeature(key: String): Feature? {
        try {
            return restClient.get()
                .uri {
                    it.pathSegment("api", "client", "features", "{environment}", "{id}")
                        .build(vividProperties.environment, key)
                }
                .headers {
                    it.accept = listOf(MediaType.APPLICATION_JSON)
                    it.add(restProperties.applicationIdHeaderName, vividProperties.applicationId)
                    restProperties.apiToken?.let { token -> it.add(restProperties.apiTokenHeaderName, token) }
                }
                .retrieve()
                .toEntity<Feature>()
                .body
        } catch (e: Exception) {
            logger.warn("Error fetching feature $key: ${e.message}")
            return null
        }
    }

    override fun fetchAllFeatures(): List<Feature>? {
        try {
            return restClient.get()
                .uri {
                    it.pathSegment("api", "client", "features", "{environment}")
                        .build(vividProperties.environment)
                }
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
