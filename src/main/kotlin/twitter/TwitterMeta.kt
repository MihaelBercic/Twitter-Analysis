package twitter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Mihael Valentin Berčič
 * on 17/08/2021 at 11:17
 * using IntelliJ IDEA
 */
@Serializable
data class TwitterMeta(
    @SerialName("next_token") val nextToken: String?,
    @SerialName("result_count") val resultCount: Int = 0
)