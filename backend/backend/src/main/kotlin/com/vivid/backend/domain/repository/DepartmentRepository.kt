package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.Department
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface DepartmentRepository : JpaRepository<Department, UUID>
