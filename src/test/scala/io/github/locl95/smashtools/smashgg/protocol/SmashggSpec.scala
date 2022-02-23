package io.github.locl95.smashtools.smashgg.protocol

import munit.CatsEffectSuite
import io.circe.syntax._
import io.circe.parser._
import io.github.locl95.smashtools.smashgg.TestHelper
import io.github.locl95.smashtools.smashgg.domain.{Entrant, Event, Participant, Phase, PlayerStanding, SmashggQuery, Tournament}
import io.github.locl95.smashtools.smashgg.protocol.Smashgg._

class SmashggSpec extends CatsEffectSuite {
  test("I can encode smash.gg queries") {
    val json = SmashggQuery.getTournamentQuery("mst-4").asJson
    assert(json.hcursor.downField("query").as[String].exists(_.contains("tournament(slug: \"mst-4\")")))
  }

  test("I can decode smash.gg tournament") {
    val tournamentJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-tournament.json")
    val expectedTournament = Tournament(312932,"MST 4")

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
    val eventJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-events.json")

    val eventFromJson =
      for {
        json <- parse(eventJson.getLines().mkString)
        event <- eventDecoder.decodeJson(json)
      } yield event

    assert(eventFromJson.contains(Event(615463, "Ultimate Singles")))
  }

  test("I can decode smash.gg entrants"){
    val eventJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-entrants.json")
    val expectedTwoFirstEntrants = List(Entrant(8348984, 615463, "Raiden's | Zandark"), Entrant(8346516, 615463, "FS | Sevro"))

    val entrantsFromJson =
      for {
        json <- parse(eventJson.getLines().mkString)
        entrants <- entrantsDecoder.decodeJson(json)
      } yield entrants

    assert(entrantsFromJson.map(_.take(2)).contains(expectedTwoFirstEntrants))
  }

  test("I can decode smash.gg standings"){
    val eventJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-standings.json")
    val expectedTwoFirstPlayers = List(PlayerStanding(1,8232866), PlayerStanding(2,8280489))

    val standingsFromJson =
      for {
        json <- parse(eventJson.getLines().mkString)
        standings <- standingsDecoder.decodeJson(json)
      } yield standings

    assert(standingsFromJson.map(_.take(2)).contains(expectedTwoFirstPlayers))
  }

  test("I can decode smash.gg phases") {
    val eventJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-phases.json")
    val expectedTwoFirstPhases = List(Phase(991477,"Bracket Pools"), Phase(991478,"Top 16"))

    val phasesFromJson =
      for {
        json <- parse(eventJson.getLines().mkString)
        phases <- phasesDecoder.decodeJson(json)
      } yield phases

    assert(phasesFromJson.map(_.take(2)).contains(expectedTwoFirstPhases))

  }

  test("I can decode smash.gg sets") {
    val eventJson = scala.io.Source.fromFile(s"src/test/resources/smashgg/smashgg-events.json")
    val expectedTwoFirstSets = TestHelper.sets

    val setsFromJson =
      for {
        json <- parse(eventJson.getLines().mkString)
        sets <- setsDecoder.decodeJson(json)
      } yield sets

    assert(setsFromJson.map(_.take(2)).contains(expectedTwoFirstSets))
  }


}
