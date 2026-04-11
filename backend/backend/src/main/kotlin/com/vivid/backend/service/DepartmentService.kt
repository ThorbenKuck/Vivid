package com.vivid.backend.service

import com.vivid.backend.domain.entity.Department
import com.vivid.backend.domain.repository.DepartmentRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class DepartmentService(
    private val departmentRepository: DepartmentRepository
) {
    fun findById(id: UUID): Department = departmentRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("Department not found with id: $id") }

    fun getAllDepartments(): List<Department> = departmentRepository.findAll()

    fun createDepartment(name: String, description: String?): Department {
        val department = Department(name = name, description = description)
        return departmentRepository.save(department)
    }
}
