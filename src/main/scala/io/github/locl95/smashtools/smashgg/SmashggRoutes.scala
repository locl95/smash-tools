package io.github.locl95.smashtools.smashgg

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import io.github.locl95.smashtools.smashgg.domain.{SmashggQuery, Tournament}
import io.github.locl95.smashtools.smashgg.service.TournamentService
import io.github.locl95.smashtools.smashgg.protocol.Smashgg._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

final class SmashggRoutes[F[_]: Async](tournamentService: TournamentService[F]) extends Http4sDsl[F]{

  val smashggRoutes: HttpRoutes[F] = {

    HttpRoutes.of[F] {
      case POST -> Root / "tournaments" / tournament =>
        for {
          t <- tournamentService.client.get[Tournament](SmashggQuery.getTournamentQuery(tournament))
          inserted <- tournamentService.insert(t)
          resp <- Ok(inserted.asJson)
        } yield resp

      case GET -> Root / "tournaments" / tournament =>
        for {
          maybeTournament <- tournamentService.getTournament(tournament)
          resp <- maybeTournament match {
            case Some(value) => Ok(value.asJson).adaptError(SmashggClientError(_))
            case None => Ok("Tournament not found".asJson)
          }
        } yield resp

      case GET -> Root / "tournaments" =>
        for {
          tournaments <- tournamentService.get
          resp <- Ok(tournaments.asJson).adaptError(SmashggClientError(_))
        } yield resp

      //case GET -> Root / "tournaments" / tournament / "events" => ???
    }
  }
}
