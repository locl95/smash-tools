package io.github.locl95.smashtools.characters.protocol

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

//TODO: Implement Kurogane Protocol: How to transform domain into json. Can be automatic for now

object Kurogane {
  implicit val kuroganeCharacterDecoder: Decoder[KuroganeCharacter] = deriveDecoder[KuroganeCharacter]
  implicit val kuroganeCharactersDecoder: Decoder[List[KuroganeCharacter]] = Decoder.decodeList[KuroganeCharacter]
  implicit def jokeEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[KuroganeCharacter]] =
    jsonOf
}
