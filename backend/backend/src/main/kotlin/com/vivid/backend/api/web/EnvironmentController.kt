package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.EnvironmentCreateRequest
import com.vivid.backend.api.web.dto.EnvironmentDto
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.PermissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
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
        @RequestParam departmentId: UUID,
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): Page<EnvironmentDto> {
        return environmentService.search(q, departmentId, pageable).map { it.toDto() }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all environments (non-paginated)")
    fun all(@RequestParam departmentId: UUID): List<EnvironmentDto> {
        return environmentService.getAll(departmentId).map { it.toDto() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new environment")
    @PreAuthorize("@permissionService.hasPermission('environments', 'write')")
    fun create(
        @RequestParam departmentId: UUID,
        @RequestBody request: EnvironmentCreateRequest
    ): EnvironmentDto = environmentService.create(departmentId, request).toDto()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete environment")
    @PreAuthorize("@permissionService.hasPermission('environments', 'write')")
    fun delete(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID
    ) {
        environmentService.delete(id, departmentId)
    }
}
