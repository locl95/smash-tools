package io.github.locl95.smashtools.characters.protocol

import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.github.locl95.smashtools.characters.TestHelper
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import munit.CatsEffectSuite
import io.github.locl95.smashtools.characters.protocol.Kurogane._

class KuroganeSpec extends CatsEffectSuite {
  test("I can transform Kurogane Characters Json") {
    val charactersJson = scala.io.Source.fromFile(s"src/test/resources/characters.json")

    val charactersFromJson = for {
      json <- parse(charactersJson.getLines().mkString)
      characters <- Decoder.decodeList[KuroganeCharacter].decodeJson(json)
    } yield characters
    assert(charactersFromJson.map(_.take(2)).contains(TestHelper.characters))
  }

  test("I can transform Kurogane Character's Moves Json") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/character-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(TestHelper.movements))
  }

  test("I can transform Characters to Json") {
    val charactersJson = List(KuroganeCharacter("Sheik"), KuroganeCharacter("Bowser")).asJson
    val expectedJson =
      s"""[
         |{"Name": "Sheik"}, {"Name": "Bowser"}
         |]""".stripMargin

    assert(parse(expectedJson).contains(charactersJson))

  }

  test("I can transform Movements to Json") {
    val charactersJson = TestHelper.movements.asJson
    val expectedJson =
      s"""[
         |  {
         |    "character" : "Joker",
         |    "name" : "Jab 1",
         |    "advantage" : -16,
         |    "type" : "ground",
         |    "firstFrame" : 4
         |  },
         |  {
         |    "character" : "Joker",
         |    "name" : "Jab 1 (Arsene)",
         |    "advantage" : null,
         |    "type" : "ground",
         |    "firstFrame" : 4
         |  }
         |]""".stripMargin

    assert(parse(expectedJson).contains(charactersJson))
  }
}
