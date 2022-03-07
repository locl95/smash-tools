package io.github.locl95.smashtools.smashgg

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import io.github.locl95.smashtools.smashgg.domain._
import io.github.locl95.smashtools.smashgg.protocol.SmashggRoutesImpl._
import io.github.locl95.smashtools.smashgg.service._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{Header, HttpRoutes}

final class SmashggRoutes[F[_]: Async](tournamentService: TournamentService[F],
                                       eventService: EventService[F],
                                       entrantService: EntrantService[F],
                                       phaseService: PhaseService[F],
                                       setsService: SetsService[F]) extends Http4sDsl[F]{

  val smashggRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "tournaments" =>

        val header: Option[Header] = req.headers.get(Authorization)
        header match {
          case Some(h) => h.value match {
            case "Bearer 3305177ceda157c60fbc09b79e2ff987" =>
              for {
                r <- req.as[Tournament]
                i <- tournamentService.insert(r)
                resp <- Ok(i.asJson)
            } yield resp

            case _ => Forbidden()
          }
          case None => Forbidden()//hauria de ser unauth
        }


      case GET -> Root / "tournaments" / tournament =>
        for {
          maybeTournament <- tournamentService.getTournament(tournament.toInt)
          resp <- maybeTournament match {
            case Some(value) => Ok(value.asJson).adaptError(SmashggClientError(_))
            case None => NotFound()
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

      case req @ POST -> Root / "events" / "entrants" =>
        for {
          entrants <- req.as[List[Entrant]]
          i <- entrantService.insert(entrants)
          resp <- Ok(i.asJson)
        } yield resp

      case GET -> Root / "events" / "entrants" =>
        for {
          entrants <- entrantService.getEntrants
          resp <- Ok(entrants.asJson).adaptError(SmashggClientError(_))
        } yield resp

      case GET -> Root / "events" / eventID / "entrants" =>
        for{
          entrants <- entrantService.getEntrants(eventID.toInt)
          resp <- Ok(entrants.asJson).adaptError(SmashggClientError(_))
        } yield resp

      case req @ POST -> Root / "phases" =>
        for {
          r <- req.as[List[Phase]]
          i <- phaseService.insert(r)
          resp <- Ok(i.asJson)
        } yield resp

      case GET -> Root / "phases" =>
        for {
          phases <- phaseService.getPhases
          resp <- Ok(phases.asJson).adaptError(SmashggClientError(_))
        } yield resp

      case req @ POST -> Root / "sets" =>
        for {
          r <- req.as[List[Sets]]
          i <- setsService.insert(r)
          resp <- Ok(i.asJson)
        } yield resp

      case GET -> Root / "sets" =>
        for {
          sets <- setsService.getSets
          resp <- Ok(sets.asJson).adaptError(SmashggClientError(_))
        } yield resp
    }
  }
}
