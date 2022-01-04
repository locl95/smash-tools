package io.github.locl95.smashtools.weeklyruleset

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}

import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Properties, Success}

object WeeklyRuleset extends App {

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

  twitterClient
    .createTweet("Hello World from Heroku")
    .andThen {
      case Success(tweet) => println(s"[WEEKLY-RULESET] Created tweet ${tweet.id} at ${tweet.created_at}")
      case Failure(exception) => println(s"[WEEKLY-RULESET] Error while creating tweet: ${exception.getMessage}")
    }(global)
}
