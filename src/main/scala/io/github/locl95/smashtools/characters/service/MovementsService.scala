package io.github.locl95.smashtools.characters.service

import cats.effect.Sync
import cats.implicits._
import io.github.locl95.smashtools.characters.KuroganeClient
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import io.github.locl95.smashtools.characters.repository.MovementsRepository

final case class MovementsService[F[_]: Sync](movementsRepository: MovementsRepository[F], client: KuroganeClient[F]) {

  def getMoves(character: String): F[List[KuroganeCharacterMove]] = {
    for {
      cached <- movementsRepository.isCached(character)
      movements <- {
        if (cached) movementsRepository.getMoves(character)
        else {
          for {
            m <- client.getMovements(character)
            _ <- movementsRepository.insert(m)
            _ <- movementsRepository.cache(character)
          } yield m
        }
      }
    } yield movements
  }

  def getMove(moveId: String): F[Option[KuroganeCharacterMove]] = {
    for {
      move <- movementsRepository.getMove(moveId)
    } yield move
  }
}
