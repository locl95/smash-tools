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
    val charactersJson = scala.io.Source.fromFile(s"src/test/resources/characters/characters.json")

    val charactersFromJson = for {
      json <- parse(charactersJson.getLines().mkString)
      characters <- Decoder.decodeList[KuroganeCharacter].decodeJson(json)
    } yield characters
    assert(charactersFromJson.map(_.take(2)).contains(TestHelper.characters))
  }

  test("I can transform Kurogane Joker's Moves Json") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/joker-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(TestHelper.movements))
  }


  test("I can transform Kurogane Wolf's Moves Json patterns") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/wolf-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(    List(
      KuroganeCharacterMove("f189d8a2b2a0485ab892310cb2149dd8", "Wolf", "Jab 1", Some(-14), "ground", Some(4)),
      KuroganeCharacterMove("0017ac939a18419191e1d2dcdb43b0a5", "Wolf", "Jab 2", Some(-14), "ground", Some(4))
    )
    ))
  }

  test("I can transform Kurogane Ken's Moves Json patterns") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/ken-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(    List(
      KuroganeCharacterMove("b369b879b66f4d4e98ea731516e35d7b", "Ken", "Light Jab 1", Some(-10), "ground", Some(3)),
      KuroganeCharacterMove("f15b5c5b6c9e4bc2b2191990cc11dd91", "Ken", "Medium Jab 1", Some(-18), "ground", Some(7))
    )
    ))
  }

  test("I can transform Kurogane Snake's Moves Json patterns") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/snake-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(    List(
      KuroganeCharacterMove("a862793940974e2484e979267459765c", "Snake", "Jab 1", None, "ground", Some(3)),
      KuroganeCharacterMove("3be8ada2afba44318cfb4b46648a6170", "Snake", "Jab 2", None, "ground", Some(4))
    )
    ))
  }


  test("I can transform Kurogane PiranhaPlant's Moves Json patterns") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/piranha-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(    List(
      KuroganeCharacterMove("33f6bbc0d9104d74918b37894b4efcc2", "PiranhaPlant", "Jab 1", Some(-13), "ground", Some(2)),
      KuroganeCharacterMove("cc79c4f18cc54b7ebe86fe0ba233c374", "PiranhaPlant", "Jab 2", Some(-15), "ground", Some(2))
    )
    ))
  }

  test("I can transform Kurogane Villager's Moves Json patterns") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/villager-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(    List(
      KuroganeCharacterMove("215be556baa64b23b53463f38d3c08b5", "Villager", "Jab 1", None, "ground", Some(3)),
      KuroganeCharacterMove("b93913abe91649c2ae93774a1d9761a8", "Villager", "Jab 2", None, "ground", Some(3))
    )
    ))
  }

  test("I can transform Kurogane Greninja's Moves Json patterns") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/characters/moves/greninja-moves.json")

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(    List(
      KuroganeCharacterMove("0357ad2838ae440ab3ed055aaa57e437", "Greninja", "Jab 1", None, "ground", Some(3)),
      KuroganeCharacterMove("445c17fb235a47528fdabe1644033b71", "Greninja", "Jab 2", None, "ground", Some(3))
    )
    ))
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
         |    "id" : "42083468d7124245b6b7f58658bb4843",
         |    "character" : "Joker",
         |    "name" : "Jab 1",
         |    "advantage" : -16,
         |    "type" : "ground",
         |    "firstFrame" : 4
         |  },
         |  {
         |    "id" : "7a25432add0345549df19a577243a983",
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
