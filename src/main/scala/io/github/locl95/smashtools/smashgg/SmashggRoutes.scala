package io.github.locl95.smashtools.smashgg

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import io.github.locl95.smashtools.smashgg.service.TournamentService
import io.github.locl95.smashtools.smashgg.protocol.Smashgg._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

final class SmashggRoutes[F[_]: Async](tournamentService: TournamentService[F]) extends Http4sDsl[F]{

  val smashggRoutes: HttpRoutes[F] = {

    HttpRoutes.of[F] {
      case GET -> Root / "tournaments" =>
        for {
          tournaments <- tournamentService.get
          resp <- Ok(tournaments.asJson).adaptError(SmashggClientError(_))
        } yield resp
    }

  }
}
