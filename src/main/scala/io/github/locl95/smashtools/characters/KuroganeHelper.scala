package io.github.locl95.smashtools.characters

import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove

object KuroganeHelper {

  def movesThatCanPunish(
      moveToPunish: KuroganeCharacterMove,
      availableMoves: List[KuroganeCharacterMove]
    ): Either[Throwable, List[KuroganeCharacterMove]] = {
    //TODO: Implement UpB. No frame penalty, but hard to detect the move from kurogane api
    //TODO: Consider Kazuya special case where aerials have +7 penalty since he has 7 jumpsquad frames
    moveToPunish
      .advantage
      .fold[Either[Throwable, List[KuroganeCharacterMove]]](Left(new Throwable("nonpunishable.move"))) {
        frameAdvantage =>
          Right(availableMoves.filter { move =>
            val foo = (move.name, move.`type`) match {
              case ("USmash", _) => move.firstFrame
              case ("Standing Grab", _) => move.firstFrame
              case (_, "ground") => move.firstFrame.map(_ + 11)
              case (_, "special") => move.firstFrame.map(_ + 3)
              case (_, "aerial") => move.firstFrame.map(_ + 3)
            }
            foo.exists(frame => frameAdvantage + frame <= 0)
          })
      }
  }
}
