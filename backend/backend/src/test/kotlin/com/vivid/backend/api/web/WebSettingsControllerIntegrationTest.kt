package com.vivid.backend.api.web

import com.vivid.backend.service.SettingsService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSettingsControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser
    fun `should get and set settings with duration as ISO-8601 string`() {
        mockMvc.perform(
            get("/api/web/settings")
                .with(jwt())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.onlineThreshold").exists())

        val json = """
            {
                "requireClientTokens": true,
                "allowDynamicClientRegistration": false,
                "onlineThreshold": "PT10M"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/web/settings")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.onlineThreshold").value("PT10M"))
            .andExpect(jsonPath("$.requireClientTokens").value(true))
    }
}
