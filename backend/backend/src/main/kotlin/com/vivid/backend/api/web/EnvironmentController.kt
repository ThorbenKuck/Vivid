package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.service.EnvironmentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/environments")
@Tag(name = "Web Environments", description = "Manage environments")
class EnvironmentController(
    private val environmentService: EnvironmentService,
) {

    @GetMapping
    @Operation(summary = "List/search environments (paginated)")
    fun list(
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): PageDto<EnvironmentDto> {
        return environmentService.search(q, pageable).toDto { it.toDto() }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all environments (non-paginated)")
    fun all(): List<EnvironmentDto> {
        return environmentService.getAll().map { it.toDto() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new environment")
    @PreAuthorize("@permissionService.hasPermission('environments', 'write')")
    fun create(
        @RequestBody request: EnvironmentCreateRequest
    ): EnvironmentDto = environmentService.create(request).toDto()

    @PatchMapping("/{id}")
    @Operation(summary = "Update environment")
    @PreAuthorize("@permissionService.hasPermission('environments', 'write')")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: EnvironmentUpdateRequest
    ): EnvironmentDto = environmentService.update(id, request).toDto()

    @PostMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reorder environments")
    @PreAuthorize("@permissionService.hasPermission('environments', 'write')")
    fun reorder(
        @RequestBody ids: List<UUID>
    ): List<EnvironmentDto> {
        return environmentService.reorder(ids).map { it.toDto() }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete environment")
    @PreAuthorize("@permissionService.hasPermission('environments', 'write')")
    fun delete(
        @PathVariable id: UUID
    ) {
        environmentService.delete(id)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get environment by ID")
    @PreAuthorize("@permissionService.hasPermission('environments', 'read')")
    fun getEnvironment(
        @PathVariable id: UUID
    ): ResponseEntity<EnvironmentDto> {
        return environmentService.findEnvironment(id.toString())
            ?.let { ResponseEntity.ok(it.toDto()) }
            ?: ResponseEntity.notFound().build()
    }
}
