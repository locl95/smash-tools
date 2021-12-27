package io.github.locl95.smashtools.characters.unit

import io.github.locl95.smashtools.characters.KuroganeHelper.movesThatCanPunish
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove
import munit.CatsEffectSuite

class KuroganeHelperSpec extends CatsEffectSuite {
  test("I can calculate which moves from one character can punish a move on shield") {
    val moveToPunish: KuroganeCharacterMove = KuroganeCharacterMove("Samus", "Jab 1", Some(-10), "ground", Some(3))

    val availableMoves: List[KuroganeCharacterMove] = List(
      KuroganeCharacterMove("Sheik", "Jab 1", Some(-12), "ground", Some(2)),
      KuroganeCharacterMove("Sheik", "Forward Tilt", Some(-15), "ground", Some(5)),
      KuroganeCharacterMove("Sheik","USmash", Some(-30), "ground", Some(11)),
      KuroganeCharacterMove("Sheik","Neutral Air", Some(-3), "aerial", Some(3)),
      KuroganeCharacterMove("Sheik","Forward Air", Some(-2), "aerial", Some(5)),
      KuroganeCharacterMove("Sheik","Standing Grab", None, "ground", Some(6)),
      KuroganeCharacterMove("Villager", "Timber Axe", None, "special", Some(6)),
      KuroganeCharacterMove("Villager","USmash", None, "ground", Some(9))
    )
    val expectedMoves: List[KuroganeCharacterMove] = List(
      KuroganeCharacterMove("Sheik","Neutral Air", Some(-3), "aerial", Some(3)),
      KuroganeCharacterMove("Sheik","Forward Air", Some(-2), "aerial", Some(5)),
      KuroganeCharacterMove("Sheik","Standing Grab", None, "ground", Some(6)),
      KuroganeCharacterMove("Villager","Timber Axe", None, "special", Some(6)),
      KuroganeCharacterMove("Villager","USmash", None, "ground", Some(9))
    )
    assert(movesThatCanPunish(moveToPunish, availableMoves).contains(expectedMoves))
  }
}
