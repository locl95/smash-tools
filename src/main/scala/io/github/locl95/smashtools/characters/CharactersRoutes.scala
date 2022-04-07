package io.github.locl95.smashtools.characters

import cats.effect.Async
import cats.implicits._
import io.circe.syntax._
import io.github.locl95.smashtools.characters.domain.KuroganeCharacterMove
import io.github.locl95.smashtools.characters.protocol.Kurogane._
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

final class CharactersRoutes[F[_]: Async](characterService: CharactersService[F], movementsService: MovementsService[F])
    extends Http4sDsl[F] {

  val characterRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "characters" =>
        for {
          characters <- characterService.get
          resp <- Ok(characters.asJson).adaptError(KuroganeClientError(_))
        } yield resp
      case GET -> Root / "characters" / character / "moves" =>
        for {
          characterMovements <- movementsService.getMoves(character.toLowerCase)
          resp <- Ok(characterMovements.asJson).adaptError(KuroganeClientError(_))
        } yield resp
      case GET -> Root / "characters" / character / "punishable-moves" =>
        for {
          characterMovements <- movementsService.getMoves(character.toLowerCase)
          resp <- Ok(characterMovements.collect {
            case a @ KuroganeCharacterMove(_, _, _, Some(adv), _, _) if adv < 0 => a
          }.asJson).adaptError(KuroganeClientError(_))
        } yield resp
      case GET -> Root / "characters" / "punishable-moves" / punishableMove / "punished-by" / character =>
        for {
          characterMoves <- movementsService.getMoves(character.toLowerCase)
          moveToPunish <- movementsService.getMove(punishableMove)
          resp <- moveToPunish.fold(NotFound())(move =>
            KuroganeHelper.movesThatCanPunish(move, characterMoves) match {
              case Left(s) => BadRequest(s.getMessage)
              case Right(l) => Ok(l.asJson)
            }
          )
        } yield resp
    }
  }
}
