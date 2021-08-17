import analysis.Sentiment
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import edu.stanford.nlp.util.PropertiesUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import twitter.Tweet
import twitter.TwitterData
import twitter.TwitterUsers
import java.io.File
import java.net.http.HttpClient
import java.util.*
import kotlin.math.roundToInt
import kotlin.system.exitProcess

val properties: Properties = PropertiesUtils.asProperties("annotators", "tokenize, ssplit, parse, sentiment")
val nlp = StanfordCoreNLP(properties)
val httpClient: HttpClient = HttpClient.newHttpClient()

fun main() {
    val scanner = Scanner(System.`in`)
    print("Twitter username: ")
    val username = scanner.nextLine()
    print("How many tweets? ")
    val count = scanner.nextInt()

    val tweets = retrieveTweets(username, count)

    if (tweets.isEmpty()) {
        println("Unable to fetch information.")
        exitProcess(1)
    }

    File("tweets.json").writeText(Json.encodeToString(tweets))

    val sentimentList = tweets.map { tweet ->
        val annotation = nlp.process(tweet.text)[SentencesAnnotation::class.java]
        val sentiment = annotation
            .map { RNNCoreAnnotations.getPredictedClass(it[SentimentCoreAnnotations.SentimentAnnotatedTree::class.java]) }
            .average()
            .roundToInt()

        println("${sentiment.asSentimentString} ${tweet.text.replace("\n", "\n\t\t\t")}")
        Sentiment(tweet, sentiment)
    }
    val average = sentimentList.map { it.score }.average().roundToInt()
    val totalTweets = tweets.size

    val veryNegativeCount = sentimentList.count { it.score == 0 }
    val negativeCount = sentimentList.count { it.score == 1 }
    val neutralCount = sentimentList.count { it.score == 2 }
    val positiveCount = sentimentList.count { it.score == 3 }
    val veryPositiveCount = sentimentList.count { it.score == 4 }

    println("\n\tOn average, the last $totalTweets tweets from user $username has been ${average.asSentimentString}.")
    println("\t\u001B[31mVERY NEGATIVE\u001B[00m $veryNegativeCount ~ ${veryNegativeCount * 100 / totalTweets}%")
    println("\t\u001B[31mNEGATIVE\u001B[00m $negativeCount ~ ${negativeCount * 100 / totalTweets}%")
    println("\t\u001B[33mNEUTRAL\u001B[00m  $neutralCount ~ ${neutralCount * 100 / totalTweets}%")
    println("\t\u001B[32mPOSITIVE\u001B[00m $positiveCount ~ ${positiveCount * 100 / totalTweets}%")
    println("\t\u001B[32mVERY POSITIVE\u001B[00m $veryPositiveCount ~ ${veryPositiveCount * 100 / totalTweets}%")

}


private fun retrieveTweets(username: String, amount: Int = 15): List<Tweet> {
    val userRequest = twitterRequest<TwitterUsers>("/users/by?usernames=$username")
    val firstUser = userRequest.data.firstOrNull() ?: throw Exception("User with the username $username does not exist.")
    val userId = firstUser.id

    val tweets = mutableListOf<Tweet>()
    var pagination: String = ""
    do {
        val query = if (pagination.isEmpty()) "" else "?pagination_token=$pagination"
        val twitterData: TwitterData = twitterRequest("/users/$userId/tweets$query")
        val meta = twitterData.meta
        if (meta.resultCount > 0) tweets.addAll(twitterData.data)

        val percentage = (tweets.size * 100.0) / amount
        print("\rRetrieving tweets... ${percentage.roundToInt()}%")

        if (meta.nextToken == null) break
        pagination = meta.nextToken
        Thread.sleep(1000)
    } while (tweets.size < amount)
    println("")
    return tweets
}