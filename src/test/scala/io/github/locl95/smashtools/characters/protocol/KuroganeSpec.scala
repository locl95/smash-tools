package io.github.locl95.smashtools.characters.protocol
import io.circe._
import io.circe.syntax._
import io.circe.parser._
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import munit.CatsEffectSuite
import io.github.locl95.smashtools.characters.protocol.Kurogane._

class KuroganeSpec extends CatsEffectSuite {
  test("I can transform Kurogane Characters Json") {
    val charactersJson = scala.io.Source.fromFile(s"src/test/resources/characters.json")
    val expectedFirstCharacters: List[KuroganeCharacter] = List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))

    val charactersFromJson = for {
      json <- parse(charactersJson.getLines().mkString)
      characters <- Decoder.decodeList[KuroganeCharacter].decodeJson(json)
    } yield characters
    assert(charactersFromJson.map(_.take(2)).contains(expectedFirstCharacters))
  }

  test("I can transform Kurogane Character's Moves Json") {
    val movementsJson = scala.io.Source.fromFile(s"src/test/resources/character-moves.json")
    val expectedFirstMovements: List[KuroganeCharacterMove] = List(KuroganeCharacterMove("Jab 1"), KuroganeCharacterMove("Jab 1 (Arsene)"))

    val movementsFromJson =
      for {
        json <- parse(movementsJson.getLines().mkString)
        movements <- Decoder.decodeList[KuroganeCharacterMove].decodeJson(json)
      } yield movements

    assert(movementsFromJson.map(_.take(2)).contains(expectedFirstMovements))
  }

  test("I can transform Characters to Json") {
    val charactersJson = List(KuroganeCharacter("Sheik"), KuroganeCharacter("Bowser")).asJson
    val expectedJson =
      s"""[
         |{"Name": "Sheik"}, {"Name": "Bowser"}
         |]""".stripMargin

    assert(parse(expectedJson).contains(charactersJson))

  }
}
