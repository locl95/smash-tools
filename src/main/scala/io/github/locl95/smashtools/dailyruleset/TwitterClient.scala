package io.github.locl95.smashtools.dailyruleset

import cats.effect.Sync
import io.github.locl95.smashtools.dailyruleset.domain.{TwitterRequest, TwitterResponse}
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Header, Headers, Request}
import io.github.locl95.smashtools.dailyruleset.protocol.Twitter._
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime

trait TwitterClient[F[_]] {
  def publishTweet(body: TwitterRequest): F[TwitterResponse]

  override def toString: String = s"TwitterClient"
}

object TwitterClient {

  def impl[F[_]: Sync](C: Client[F]): TwitterClient[F] = new TwitterClient[F] {

    /*
    API KEY: gVFZEZ4tOA9YeYqRW06ulMpzf
    API KEY SECRET: xox1wpH7cdP3JJprdKFGLA8qMVFoP9Z2RuW4k15Lz9kF7NPdn2
    BEARER TOKEN: AAAAAAAAAAAAAAAAAAAAAADcXgEAAAAAlSlgcg5%2BTlL%2B3zMZMgUaD4pZGAE%3DKjKha1Jy28YaYSEYU5nYWdet7NJfFSHXTZMqlq8OxFF0zODygu
    ACCESS TOKEN: 1477990764957384710-KX98z2E0rmp68qQMEJr3j4V93U5oxi
    ACCESS TOKEN SECRET: a0IM8ClNgLkGILnmYqkYtvnMKDywgmWNv4nWrq44rdUPr
     */

    override def publishTweet(body: TwitterRequest): F[TwitterResponse] = {
      val oauthConsumerKey = "epLOLHHUtfHERO3P4LVIKve3j"
      val oauthSignatureMethod = "HMAC-SHA1"
      val oauthVersion = "1.0"
      val oauthToken = "1477990764957384710-jvgkBhJuU986hl7abGjtiaWlKSsTjA"
      val oauthNonce = Base64.encodeBase64String(body.text.getBytes)
      val oauthTimestamp = DateTime.now().getMillis.toString

      val paramsToSign: List[(String, String)] = List(
        "status" -> body.text,
        "include_entities" -> "true",
        "oauth_consumer_key" -> oauthConsumerKey,
        "oauth_nonce" -> oauthNonce,
        "oauth_signature_method" -> oauthSignatureMethod,
        "oauth_timestamp" -> oauthTimestamp,
        "oauth_token" -> oauthToken,
        "oauth_version" -> oauthVersion
      )

      val oAuthParams: List[(String, String)] = List(
        "oauth_consumer_key" -> oauthConsumerKey,
        "oauth_nonce" -> oauthNonce,
        "oauth_signature" -> TwitterHelper.signed(
          paramsToSign,
          "POST",
          "https://api.twitter.com/2/tweets",
          "KqTUgUWmdpMXBWhSV5nlAwyGZm6R6d4EjvqOXeG1EUiqLm68eJ",
          "qehXotC8cxGesI4MixLW7nHFk31L8wewkYaBY1au0rj2A"
        ),
        "oauth_signature_method" -> oauthSignatureMethod,
        "oauth_timestamp" -> oauthTimestamp,
        "oauth_token" -> oauthToken,
        "oauth_version" -> oauthVersion
      )

      val headers = Headers.apply(
        Header(
          "Authorization",
          TwitterHelper.oAuthHeader(oAuthParams)
        )
      )

      val request =
        Request[F](method = POST, uri = uri"https://api.twitter.com/2/tweets", headers = headers).withEntity(body)

      C.expect[TwitterResponse](request)
    }

  }
}
