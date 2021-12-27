package io.github.locl95.smashtools.characters.service

import cats.effect.Sync
import cats.implicits._
import io.github.locl95.smashtools.UriHelper
import io.github.locl95.smashtools.characters.KuroganeClient
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import io.github.locl95.smashtools.characters.repository.MovementsRepository

final case class MovementsService[F[_]: Sync](movementsRepository: MovementsRepository[F], client: KuroganeClient[F]) {

  def get(character: String): F[List[KuroganeCharacterMove]] = {
    for {
      cached <- movementsRepository.isCached
      movements <- {
        if (cached) movementsRepository.get
        else {
          for {
            uri <- UriHelper.fromString(s"https://api.kuroganehammer.com/api/characters/name/$character/moves?expand=true&game=ultimate")
            m <- client.get[List[KuroganeCharacterMove]](uri)
            _ <- movementsRepository.insert(m)
            _ <- movementsRepository.cache
          } yield m
        }
      }
    } yield movements
  }
}
