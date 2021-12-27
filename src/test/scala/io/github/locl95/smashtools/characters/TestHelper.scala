package io.github.locl95.smashtools.characters

import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}

object TestHelper {
  val characters: List[KuroganeCharacter] =
    List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))

  val movements: List[KuroganeCharacterMove] =
    List(
      KuroganeCharacterMove("Joker", "Jab 1", Some(-16), "ground", Some(4)),
      KuroganeCharacterMove("Joker", "Jab 1 (Arsene)", None, "ground", Some(4))
    )
}
