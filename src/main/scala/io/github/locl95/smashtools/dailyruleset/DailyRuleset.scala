package io.github.locl95.smashtools.dailyruleset

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Properties, Random, Success}

object DailyRuleset extends App {

  private val consumerToken = Properties.envOrNone("RULESET_CONSUMER_TOKEN")
  private val consumerSecret = Properties.envOrNone("RULESET_CONSUMER_SECRET")
  private val accessToken = Properties.envOrNone("RULESET_ACCESS_TOKEN")
  private val accessTokenSecret = Properties.envOrNone("RULESET_ACCESS_TOKEN_SECRET")

  val twitterClient = (consumerToken, consumerSecret, accessToken, accessTokenSecret) match {
    case (Some(ct), Some(cs), Some(at), Some(ats)) =>
      TwitterRestClient(
        ConsumerToken(ct, cs),
        AccessToken(at, ats)
      )
    case _ => throw new Exception("Error while gettint Twtitter environment variables")
  }

  val stagesStartersPool: List[String] =
    List(
      "Battlefield",
      "Small Battlefield",
      "Final Destination",
      "Pokemon Stadium 2",
      "Smashville",
      "Kalos Pokemon League",
      "Town and City",
      "Hollow Bastion"
    )

  val numberOfStarters = Random.between(3, 6)

  val stagesCounterPicksPool: List[String] =
    List(
      "Kongo Jungle",
      "Dream Land",
      "Brinstar",
      "Yoshi's Story",
      "Fountain of Dreams",
      "Frigate Orpheon",
      "Halberd",
      "Lylat Cruise",
      "Castle Siege",
      "Unova Pokemon League",
      "Prism Tower",
      "Duck Hunt",
      "Wuhu Island"
    )

  val numberOfCounterPicks = Random.between(2, 6)

  val forbiddenCharacters: List[String] = List("Steve", "Sonic", "Samus", "Ken", "Luigi")
  val numberOfForbiddenCharacters = Random.between(1, 2)

  val now = DateTime.now

  val twitterBody: String =
    s"Ruleset Diario ${now.toString("dd-MM-yyyy")}:\nStarters: ${Random.shuffle(stagesStartersPool).take(numberOfStarters).mkString(", ")}\nCounterPicks: ${Random
      .shuffle(stagesCounterPicksPool)
      .take(numberOfCounterPicks)
      .mkString(", ")}\nBans: ${(numberOfStarters + numberOfCounterPicks) / 2 + 1}\nPersonajes Baneados: ${Random.shuffle(forbiddenCharacters).take(numberOfForbiddenCharacters).mkString(", ")}"

  twitterClient
    .createTweet(twitterBody)
    .onComplete {
      case Success(tweet) =>
        println(s"[WEEKLY-RULESET] Created tweet ${tweet.id} at ${tweet.created_at}")
        sys.exit(0)
      case Failure(exception) =>
        println(s"[WEEKLY-RULESET] Error while creating tweet: ${exception.getMessage}")
        sys.exit(0)
    }
}
