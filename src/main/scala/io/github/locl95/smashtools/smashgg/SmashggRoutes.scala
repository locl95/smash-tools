package io.github.locl95.smashtools.smashgg

import cats.effect.Async
import cats.implicits._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.smashgg.domain.Tournament
import io.github.locl95.smashtools.smashgg.service.TournamentService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

final class SmashggRoutes[F[_]: Async](tournamentService: TournamentService[F]) extends Http4sDsl[F]{

  implicit private val modifiedDecoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
  implicit private val modifiedEntityDecoderForTournament: EntityDecoder[F, Tournament] =
    jsonOf
  implicit private val encoderForTournament: Encoder[Tournament] = deriveEncoder[Tournament]

  val smashggRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root / "tournaments" =>
        for {
          r <- req.as[Tournament]
          i <- tournamentService.insert(r)
          resp <- Ok(i.asJson)
        } yield resp

//      case POST -> Root / "tournaments" / tournament => {
//        val program: fs2.Stream[F, Tournament] = for {
//          tournament <- fs2
//            .Stream
//            .eval(tournamentService.client.get[Tournament](SmashggQuery.getTournamentQuery(tournament)))
//        } yield tournament
//
//        val apiTournament: F[Tournament] = program.compile.lastOrError
//
//        for {
//          t <- apiTournament
//          i <- tournamentService.insert(t)
//          resp <- Ok(i.asJson)
//      } yield resp
//    }

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
