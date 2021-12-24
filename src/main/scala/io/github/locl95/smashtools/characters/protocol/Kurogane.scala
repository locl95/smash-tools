package io.github.locl95.smashtools.characters.protocol

import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

//TODO: Implement Kurogane Protocol: How to transform domain into json. Can be automatic for now

object Kurogane {
  implicit val kuroganeCharacterDecoder: Decoder[KuroganeCharacter] = deriveDecoder[KuroganeCharacter]
  implicit val kuroganeCharactersDecoder: Decoder[List[KuroganeCharacter]] = Decoder.decodeList[KuroganeCharacter]
  implicit def KuroganeCharactersDecoder[F[_]: Sync]: EntityDecoder[F, List[KuroganeCharacter]] =
    jsonOf
  implicit val kuroganeMovementDecoder: Decoder[KuroganeCharacterMove] = deriveDecoder[KuroganeCharacterMove]

  implicit val kuroganeCharacterEncoder: Encoder[KuroganeCharacter] = deriveEncoder[KuroganeCharacter]
  implicit val kuroganeCharactersEncoder: Encoder[List[KuroganeCharacter]] = Encoder.encodeList[KuroganeCharacter]
}
