import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Created by Mihael Valentin Berčič
 * on 17/08/2021 at 11:46
 * using IntelliJ IDEA
 */

/** Returns colored word for console output based. */
val Int.asSentimentString
    get() = when (this) {
        0 -> "\u001b[31mVERY NEGATIVE\u001b[00m"
        1 -> "\u001b[31mNEGATIVE\u001b[00m"
        2 -> "\u001b[33mNEUTRAL\u001b[00m"
        3 -> "\u001b[32mPOSITIVE\u001b[00m"
        else -> "\u001b[32mVERY POSITIVE\u001b[00m"
    }.padEnd(20, ' ')


const val token = "AAAAAAAAAAAAAAAAAAAAAAP0PgEAAAAAk9i4nfRk4P8E5bTZfFZitqXuliE%3Dj8gTw6qWoTW0qNm5YKOi28yrjBb5S0V5nSG5nIRTFGQ6OmM0xD"

/** Performs HTTP GET request to twitter API using [token] for authentication. */
inline fun <reified T> twitterRequest(endPoint: String): T {
    val url = URL("https://api.twitter.com/2$endPoint")
    try {
        val request = HttpRequest
            .newBuilder()
            .uri(url.toURI())
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .headers("Authorization", "Bearer $token", "Content-Type", "application/json")
            .build()

        val response = httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .get()

        return Json { ignoreUnknownKeys = true }.decodeFromString(response.body())

    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}