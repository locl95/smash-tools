package io.github.locl95.smashtools.smashgg

import cats.effect.Async
import cats.implicits._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.smashgg.domain.{Event, Tournament}
import io.github.locl95.smashtools.smashgg.service.{EventService, TournamentService}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

final class SmashggRoutes[F[_]: Async](tournamentService: TournamentService[F],
                                       eventService: EventService[F]) extends Http4sDsl[F]{

  implicit private val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
  implicit private val entityDecoderForTournament: EntityDecoder[F, Tournament] =
    jsonOf
  implicit private val encoderForTournament: Encoder[Tournament] = deriveEncoder[Tournament]

  implicit private val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
  implicit private val entityDecoderForEvent: EntityDecoder[F, Event] =
    jsonOf
  implicit private val encoderForEvent: Encoder[Event] = deriveEncoder[Event]

  val smashggRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "tournaments" =>
        for {
          r <- req.as[Tournament]
          i <- tournamentService.insert(r)
          resp <- Ok(i.asJson)
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

      case req @ POST -> Root / "tournaments" / tournamentID / "events" =>
        for {
          r <- req.as[Event]
          i <- eventService.insert(tournamentID.toInt, r)
          resp <- Ok(i.asJson)
        } yield resp

      case GET -> Root / "events" =>
        for {
          events <- eventService.get
          resp <- Ok(events.asJson).adaptError(SmashggClientError(_))
        } yield resp

      case GET -> Root / "tournaments" / tournamentID / "events" =>
        for {
          events <- eventService.getEvents(tournamentID.toInt)
          resp <- Ok(events.asJson).adaptError(SmashggClientError(_))
        } yield resp
    }
  }
}
