package io.github.locl95.smashtools.characters

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import io.github.locl95.smashtools.characters.service.CharactersService
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

final class CharactersRoutes[F[_]: Async](service: CharactersService[F]) extends Http4sDsl[F] {

  val characterRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "characters" =>
        for {
          characters <- service.getCharacters
          resp <- Ok(characters.asJson).adaptError(KuroganeClientError(_))
        } yield resp
    }
  }
}
