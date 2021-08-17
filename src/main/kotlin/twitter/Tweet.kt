package twitter

import kotlinx.serialization.Serializable

/**
 * Created by Mihael Valentin Berčič
 * on 17/08/2021 at 11:18
 * using IntelliJ IDEA
 */
@Serializable
data class Tweet(val id: String, val text: String)