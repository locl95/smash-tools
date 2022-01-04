package io.github.locl95.smashtools.characters.service

import cats.effect.Sync
import cats.implicits._
import io.github.locl95.smashtools.characters.KuroganeClient
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter
import io.github.locl95.smashtools.characters.repository.CharactersRepository
import io.github.locl95.smashtools.characters.protocol.Kurogane._

final case class CharactersService[F[_]: Sync](
    charactersRepository: CharactersRepository[F],
    client: KuroganeClient[F]
  ) {

  def get: F[List[KuroganeCharacter]] = {
    for {
      cached <- charactersRepository.isCached
      characters <- {
        if (cached) charactersRepository.get
        else {
          for {
            c <- client.getCharacters
            _ <- charactersRepository.insert(c)
            _ <- charactersRepository.cache
          } yield c
        }
      }
    } yield characters
  }
}
