package io.github.locl95.smashtools.dailyruleset.protocol

import io.circe.parser._
import io.circe.syntax._
import io.github.locl95.smashtools.dailyruleset.domain.{TwitterRequest, TwitterResponse}
import io.github.locl95.smashtools.dailyruleset.protocol.Twitter._
import munit.CatsEffectSuite

class TwitterSpec extends CatsEffectSuite {
  test("I can decode Twitter Response") {
    val twitterResponse = scala.io.Source.fromFile(s"src/test/resources/weeklyruleset/twitter-response.json")

    val twitterResponseFromJson = for {
      json <- parse(twitterResponse.getLines().mkString)
      characters <- twitterResponseDecoder.decodeJson(json)
    } yield characters
    assert(twitterResponseFromJson.contains(TwitterResponse("1445880548472328192", "Hello world!")))
  }
  test("I can transform TwitterRequest to Json") {
    val twitterRequest = TwitterRequest("Hello World!").asJson
    val expectedJson =
      s"""{"text": "Hello World!"}"""

    assert(parse(expectedJson).contains(twitterRequest))

  }

}
