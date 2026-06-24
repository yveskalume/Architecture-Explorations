package dev.yveskalume.newsapp.data.network.model

import dev.yveskalume.newsapp.domain.model.Source
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String
)

fun SourceDto.toDomain(): Source {
    return Source(
        id = id,
        name = name
    )
}