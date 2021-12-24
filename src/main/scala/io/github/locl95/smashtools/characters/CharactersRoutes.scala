package io.github.locl95.smashtools.characters

import cats.effect.Sync
import cats.implicits._
import io.github.locl95.smashtools.UriHelper
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._
import org.http4s.circe._


object CharactersRoutes {

  def characterRoutes[F[_]: Sync](K: KuroganeClient[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "characters" =>
        for {
          uri <- UriHelper.fromString("https://api.kuroganehammer.com/api/characters?game=ultimate")
          characters <- K.get[List[KuroganeCharacter]](uri)
          resp <- Ok(characters.asJson).adaptError(KuroganeClientError(_))
        } yield resp
    }
  }
}
