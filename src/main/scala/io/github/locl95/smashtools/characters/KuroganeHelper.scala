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
      .fold[Either[Throwable, List[KuroganeCharacterMove]]](Left(new Throwable("not.punishable.move"))) {
        frameAdvantage =>
          Right(availableMoves.filter { move =>
            val framesAfterPenalty = (move.name, move.`type`) match {
              case ("USmash", _) => move.firstFrame
              case ("Standing Grab", _) => move.firstFrame.map(_ + 4)
              case (_, "ground") => move.firstFrame.map(_ + 11)
              case (_, "special") => move.firstFrame.map(_ + 3)
              case (_, "aerial") => move.firstFrame.map(_ + 3)
              case (_, _) => None
            }
            //      KuroganeCharacterMove("7a25432add0345549df19a577243a983", "Joker", "Jab 1 (Arsene)", None, "ground", Some(4))
            framesAfterPenalty.exists(frame => frameAdvantage + frame <= 0)
          })
      }
  }
}
