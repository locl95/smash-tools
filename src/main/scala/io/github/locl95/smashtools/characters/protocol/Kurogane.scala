package io.github.locl95.smashtools.characters.protocol

import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe._
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

//TODO: Implement Kurogane Protocol: How to transform domain into json. Can be automatic for now

object Kurogane {
  implicit val kuroganeCharacterDecoder: Decoder[KuroganeCharacter] = deriveDecoder[KuroganeCharacter]
  implicit val kuroganeCharactersDecoder: Decoder[List[KuroganeCharacter]] = Decoder.decodeList[KuroganeCharacter]
  implicit def kuroganeCharactersEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[KuroganeCharacter]] =
    jsonOf
  implicit val kuroganeCharacterEncoder: Encoder[KuroganeCharacter] = deriveEncoder[KuroganeCharacter]
  implicit val kuroganeCharactersEncoder: Encoder[List[KuroganeCharacter]] = Encoder.encodeList[KuroganeCharacter]

  implicit val kuroganeMovementDecoder: Decoder[KuroganeCharacterMove] = (c: HCursor) => {
    def avoidNullHitbox(json: Json): Json =
      if (json.isNull) Json.fromJsonObject(JsonObject("Frames" -> Json.Null, "Adv" -> Json.Null)) else json

    val activeFramesRegex = "^([0-9]+)-[0-9]+$".r
    val commaSeparatedFramesRegex = "^([0-9]+)(, [0-9]+)+$".r
    val noFrameSpecified = "^(.*:)+$".r
    val wordsAndThenFrameBetweenWordsParenthesis = "^.*\\(.*([0-9]+).*\\)$".r
    val wordsAndThenFrameBetweenParenthesis = "^.*: ([0-9]+)- \\(.*: [0-9]+-[0-9]+\\)$".r
    val frameAndThenWords = "^([0-9]+).*$".r
    val framesAndThenWords = "^([0-9]+)-[0-9]+.*$".r
    val wordsAndThenFrames = "^.*: ([0-9]+).*+$".r
    val wordsAndThenFrame = "^.*: ([0-9]+)+$".r

    for {
      id <- c.downField("InstanceId").as[String]
      owner <- c.downField("Owner").as[String]
      name <- c.downField("Name").as[String]
      activeFrames <- c.downField("HitboxActive").withFocus(avoidNullHitbox).downField("Frames").as[Option[String]]
      advantage <- c.downField("HitboxActive").withFocus(avoidNullHitbox).downField("Adv").as[Option[String]]
      moveType <- c.downField("MoveType").as[String]
    } yield {
      KuroganeCharacterMove(
        id,
        owner,
        name,
        advantage match {
          case Some("") => None
          case s => s.map(_.toInt)
        },
        moveType,
        activeFrames match {
          case Some("") => None
          case Some("?") => None
          case Some("-") => None
          case Some(noFrameSpecified(_)) => None
          case Some(wordsAndThenFrameBetweenParenthesis(f)) => Some(f.toInt)
          case Some(activeFramesRegex(f)) => Some(f.toInt)
          case Some(commaSeparatedFramesRegex(f, _)) => Some(f.toInt)
          case Some(frameAndThenWords(f)) => Some(f.toInt)
          case Some(framesAndThenWords(f)) => Some(f.toInt)
          case Some(wordsAndThenFrame(f)) => Some(f.toInt)
          case Some(wordsAndThenFrames(f)) => Some(f.toInt)
          case Some(wordsAndThenFrameBetweenWordsParenthesis(f)) => Some(f.toInt)
          case s => s.map(_.toInt)
        }
      )
    }
  }
  implicit val kuroganeMovementsDecoder: Decoder[List[KuroganeCharacterMove]] = Decoder.decodeList[KuroganeCharacterMove]
  implicit def kuroganeMovementsEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[KuroganeCharacterMove]] =
    jsonOf
  implicit val kuroganeMovementEncoder: Encoder[KuroganeCharacterMove] = deriveEncoder[KuroganeCharacterMove]
  implicit val kuroganeMovementsEncoder: Encoder[List[KuroganeCharacterMove]] = Encoder.encodeList[KuroganeCharacterMove]
}
