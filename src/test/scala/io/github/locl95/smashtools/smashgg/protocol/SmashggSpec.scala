package io.github.locl95.smashtools.smashgg.protocol

import munit.CatsEffectSuite
import io.circe.syntax._
import io.circe.parser._
import io.github.locl95.smashtools.smashgg.domain.{Event, Participant, SmashggQuery, Tournament}
import io.github.locl95.smashtools.smashgg.protocol.Smashgg._

class SmashggSpec extends CatsEffectSuite {
  test("I can encode smash.gg queries") {
    val json = SmashggQuery.getTournamentQuery("mst-4").asJson
    assert(json.hcursor.downField("query").as[String].exists(_.contains("tournament(slug: \"mst-4\")")))
  }
  test("I can decode smash.gg tournament") {
    val tournamentJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-tournament.json")
    val expectedTournament = Tournament("MST 4")

    val tournamentFromJson =
      for {
        json <- parse(tournamentJson.getLines().mkString)
        tournament <- tournamentDecoder.decodeJson(json)
      } yield tournament

    assert(tournamentFromJson.contains(expectedTournament))

  }

  test("I can decode smash.gg participants") {
    val participants = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-participants.json")
    val expectedFirstParticipants = List(
      Participant(List(8022537)),
      Participant(List(7914930)),
      Participant(List(7919929, 7914930))
    )

    val participantsFromJson =
      for {
        json <- parse(participants.getLines().mkString)
        participants <- participantsDecoder.decodeJson(json)
      } yield participants

    assert(participantsFromJson.map(_.take(3)).contains(expectedFirstParticipants))

  }

  test("I can decode smash.gg events") {
    val eventJson = scala.io.Source.fromFile(s"src/test/resources/smashgg-events.json")

    val eventFromJson =
      for {
        json <- parse(eventJson.getLines().mkString)
        event <- eventDecoder.decodeJson(json)
      } yield event

    assert(eventFromJson.contains(Event("Ultimate Singles")))

  }
}
