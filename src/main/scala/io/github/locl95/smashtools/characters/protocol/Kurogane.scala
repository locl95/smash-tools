package io.github.locl95.smashtools.characters.protocol

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}

//TODO: Implement Kurogane Protocol: How to transform domain into json. Can be automatic for now

object Kurogane {
  implicit val kuroganeCharacterDecoder: Decoder[KuroganeCharacter] = deriveDecoder[KuroganeCharacter]
  implicit val kuroganeMovementDecoder: Decoder[KuroganeCharacterMove] = deriveDecoder[KuroganeCharacterMove]
}
