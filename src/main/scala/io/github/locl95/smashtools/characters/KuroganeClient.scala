package io.github.locl95.smashtools.characters

import cats.effect.Sync
import io.github.locl95.smashtools.UriHelper
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import org.http4s.EntityDecoder
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits.http4sLiteralsSyntax
import cats.implicits._

//TODO: Implement KuroganeClient. Should be able to retreat Characters, CharactersMoves and Moves

trait KuroganeClient[F[_]] {
  def getCharacters(implicit decoder: EntityDecoder[F, List[KuroganeCharacter]]): F[List[KuroganeCharacter]]

  def getMovements(
      character: String
    )(
      implicit decoder: EntityDecoder[F, List[KuroganeCharacterMove]]
    ): F[List[KuroganeCharacterMove]]

  override def toString: String = s"KuroganeClient"
}

case class KuroganeClientError(t: Throwable) extends RuntimeException

object KuroganeClient {

  def impl[F[_]: Sync](C: Client[F]): KuroganeClient[F] = new KuroganeClient[F] {
    val dsl: Http4sClientDsl[F] = new Http4sClientDsl[F] {}
    import dsl._

    override def getCharacters(
        implicit decoder: EntityDecoder[F, List[KuroganeCharacter]]
      ): F[List[KuroganeCharacter]] =
      C.expect[List[KuroganeCharacter]](
        GET(uri"https://api.kuroganehammer.com/api/characters?game=ultimate")
      )

    override def getMovements(
        character: String
      )(
        implicit decoder: EntityDecoder[F, List[KuroganeCharacterMove]]
      ): F[List[KuroganeCharacterMove]] =
      for {
        uri <- UriHelper.fromString(
          s"https://api.kuroganehammer.com/api/characters/name/$character/moves?expand=true&game=ultimate"
        )
        result <- C.expect[List[KuroganeCharacterMove]](uri)
      } yield result

  }
}
