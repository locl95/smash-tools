package io.github.locl95.smashtools.characters.service

import cats.effect.Sync
import cats.implicits._
import io.github.locl95.smashtools.UriHelper
import io.github.locl95.smashtools.characters.KuroganeClient
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import io.github.locl95.smashtools.characters.repository.CharactersRepository


final case class CharactersService[F[_]: Sync](charactersRepository: CharactersRepository[F], client: KuroganeClient[F]) {

  def get: F[List[KuroganeCharacter]] = {
    for {
      cached <- charactersRepository.isCached
      characters <- {
        if (cached) charactersRepository.get
        else {
          for {
            uri <- UriHelper.fromString("https://api.kuroganehammer.com/api/characters?game=ultimate")
            c <- client.get[List[KuroganeCharacter]](uri)
            _ <- charactersRepository.insert(c)
            _ <- charactersRepository.cache
          } yield c
        }
      }
    } yield characters
  }
}
