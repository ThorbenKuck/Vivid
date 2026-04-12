package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.DepartmentCreateRequest
import com.vivid.backend.api.web.dto.DepartmentDto
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.DepartmentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/departments")
@Tag(name = "Web Department Management", description = "Manage departments")
class DepartmentController(
    private val departmentService: DepartmentService
) {
    @GetMapping
    @Operation(summary = "Get all departments")
    @PreAuthorize("@permissionService.hasPermission('departments', 'read')")
    fun getAllDepartments(): List<DepartmentDto> {
        return departmentService.getAllDepartments().map { it.toDto() }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    @PreAuthorize("@permissionService.hasPermission('departments', 'read')")
    fun getDepartmentById(@PathVariable id: UUID): DepartmentDto {
        return departmentService.findById(id).toDto(includeTeams = true)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new department")
    @PreAuthorize("@permissionService.hasPermission('departments', 'write')")
    fun createDepartment(@RequestBody request: DepartmentCreateRequest): DepartmentDto {
        return departmentService.createDepartment(request.name, request.description).toDto()
    }
}
