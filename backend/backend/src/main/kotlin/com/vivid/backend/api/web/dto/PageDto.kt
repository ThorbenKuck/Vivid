package com.vivid.backend.api.web.dto

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class PageDto<T: Any>(
    val content: List<T>,
    val totalCount: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val pageable: PageableDto,
) {
    fun <S: Any>map(transform: (T) -> S): PageDto<S> = PageDto(
        content = content.map(transform),
        totalCount = totalCount,
        totalPages = totalPages,
        first = first,
        last = last,
        pageable = pageable,
    )
}

fun <T: Any> Page<T>.toDto(): PageDto<T> = PageDto(
    content = content,
    totalCount = totalElements,
    totalPages = totalPages,
    first = isFirst,
    last = isLast,
    pageable = pageable.toDto(),
)

fun <S: Any, T: Any> Page<T>.toDto(transformer: (T) -> S): PageDto<S> = toDto().map(transformer)

data class PageableDto(
    val page: Int,
    val size: Int,
    val sort: List<SortDto>,
) {
    fun toDomain(): Pageable = PageRequest.of(page, size, Sort.by(sort.map { it.toDomain() }))
}

fun Pageable.toDto(): PageableDto = PageableDto(pageNumber, pageSize, sort.toDto())

data class SortDto(
    val key: String,
    val direction: Direction?,
) {
    enum class Direction { ASC, DESC }

    fun toDomain(): Sort.Order {
        return when (direction) {
            Direction.ASC -> Sort.Order.asc(key)
            Direction.DESC -> Sort.Order.desc(key)
            else -> Sort.Order.by(key)
        }
    }
}

fun Sort.toDto(): List<SortDto> {
    return map { SortDto(it.property, it.direction.toDto()) }.toList()
}

fun Sort.Direction.toDto(): SortDto.Direction {
    return when(this) {
        Sort.Direction.ASC -> SortDto.Direction.ASC
        Sort.Direction.DESC -> SortDto.Direction.DESC
    }
}