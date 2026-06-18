package dev.yveskalume.newsappp.data.network.model

import dev.yveskalume.newsappp.domain.model.SourceItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourceItemDto(
    @SerialName("id")
    val id: String?,
    @SerialName("name")
    val name: String?,
    @SerialName("description")
    val description: String?,
    @SerialName("url")
    val url: String?,
    @SerialName("category")
    val category: String?,
    @SerialName("language")
    val language: String?,
    @SerialName("country")
    val country: String?
)

fun SourceItemDto.toDomain(): SourceItem? {
    return SourceItem(
        id = id ?: return null,
        name = name ?: return null,
        description = description.orEmpty(),
        url = url.orEmpty(),
        category = category.orEmpty(),
        language = language.orEmpty(),
        country = country.orEmpty()
    )
}