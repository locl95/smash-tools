package io.github.locl95.smashtools.characters

import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}

object TestHelper {
  val characters: List[KuroganeCharacter] =
    List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))

  val movements: List[KuroganeCharacterMove] =
    List(
      KuroganeCharacterMove("42083468d7124245b6b7f58658bb4843", "Joker", "Jab 1", Some(-16), "ground", Some(4)),
      KuroganeCharacterMove("7a25432add0345549df19a577243a983", "Joker", "Jab 1 (Arsene)", None, "ground", Some(4))
    )
}
