package io.github.locl95.smashtools.characters.service

import cats.effect.Sync
import io.github.locl95.smashtools.UriHelper
import io.github.locl95.smashtools.characters.KuroganeClient
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import io.github.locl95.smashtools.characters.repository.CharactersRepository
import cats.implicits._

trait Service[F[_]] {
  def getCharacters: F[List[KuroganeCharacter]]
}

final case class CharactersService[F[_]: Sync](charactersRepository: CharactersRepository[F], client: KuroganeClient[F])
    extends Service[F] {

  override def getCharacters: F[List[KuroganeCharacter]] = {
    for {
      uri <- UriHelper.fromString("https://api.kuroganehammer.com/api/characters?game=ultimate")
      cached <- charactersRepository.isCached("characters")
      characters <- {
        if (cached) charactersRepository.get
        else {
          for {
            c <- client.get[List[KuroganeCharacter]](uri)
            _ <- charactersRepository.insert(c)
            _ <- charactersRepository.cache("characters")
          } yield c
        }
      }
    } yield characters
  }
}
