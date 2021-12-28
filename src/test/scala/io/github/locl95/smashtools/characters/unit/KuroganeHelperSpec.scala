package io.github.locl95.smashtools.characters.unit

import io.github.locl95.smashtools.characters.KuroganeHelper.movesThatCanPunish
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove
import munit.CatsEffectSuite

class KuroganeHelperSpec extends CatsEffectSuite {
  test("I can calculate which moves from one character can punish a move on shield") {
    val moveToPunish: KuroganeCharacterMove = KuroganeCharacterMove("202c6c46ac7c43b6afa0937f03c7caab", "Samus", "Jab 1", Some(-10), "ground", Some(3))

    val availableMoves: List[KuroganeCharacterMove] = List(
      KuroganeCharacterMove("6a30c73c7a5449298f6e598250fc71cd","Sheik", "Jab 1", Some(-12), "ground", Some(2)),
      KuroganeCharacterMove("b3d4ce1bbdc74dacafc74502817dd1a6", "Sheik", "Forward Tilt", Some(-15), "ground", Some(5)),
      KuroganeCharacterMove("9d6e5c41575c4ca182f8ac157c226eb5","Sheik","USmash", Some(-30), "ground", Some(11)),
      KuroganeCharacterMove("1f09b8358acc44fcbd1425c9ea84b28a", "Sheik","Neutral Air", Some(-3), "aerial", Some(3)),
      KuroganeCharacterMove("93efaf3af8de4359b644e383dd4f7b12", "Sheik","Forward Air", Some(-2), "aerial", Some(5)),
      KuroganeCharacterMove("f943c0fcd3b541679a68e67e06d1ced8", "Sheik","Standing Grab", None, "ground", Some(6)),
      KuroganeCharacterMove("82369d7a3e144da9bf04a96cdcaef62f", "Villager", "Timber Axe", None, "special", Some(6)),
      KuroganeCharacterMove("4f567a692d8f4229b082a4b8dd9f424c","Villager","USmash", None, "ground", Some(9))
    )
    val expectedMoves: List[KuroganeCharacterMove] = List(
      KuroganeCharacterMove("1f09b8358acc44fcbd1425c9ea84b28a", "Sheik","Neutral Air", Some(-3), "aerial", Some(3)),
      KuroganeCharacterMove("93efaf3af8de4359b644e383dd4f7b12", "Sheik","Forward Air", Some(-2), "aerial", Some(5)),
      KuroganeCharacterMove("f943c0fcd3b541679a68e67e06d1ced8", "Sheik","Standing Grab", None, "ground", Some(6)),
      KuroganeCharacterMove("82369d7a3e144da9bf04a96cdcaef62f", "Villager", "Timber Axe", None, "special", Some(6)),
      KuroganeCharacterMove("4f567a692d8f4229b082a4b8dd9f424c","Villager","USmash", None, "ground", Some(9))
    )
    assert(movesThatCanPunish(moveToPunish, availableMoves).contains(expectedMoves))
  }
}
