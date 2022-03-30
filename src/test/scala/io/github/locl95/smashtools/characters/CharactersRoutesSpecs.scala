package io.github.locl95.smashtools.characters

import cats.effect.{IO, Resource}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import io.github.locl95.smashtools.characters.protocol.Kurogane.kuroganeCharactersEntityDecoder
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import munit.CatsEffectSuite
import org.http4s.circe.jsonOf
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{EntityDecoder, Method, Request, Status}

class CharactersRoutesSpecs extends CatsEffectSuite {

  private val modifiedDecoderForMove: Decoder[KuroganeCharacterMove] =
    deriveDecoder[KuroganeCharacterMove]

  implicit private val modifiedDecoderForMoves: Decoder[List[KuroganeCharacterMove]] =
    Decoder.decodeList[KuroganeCharacterMove](modifiedDecoderForMove)
  private val modifiedEntityDecoderForMoves: EntityDecoder[IO, List[KuroganeCharacterMove]] = jsonOf
  private val kc: KuroganeClient[IO] = new KuroganeClientMock[IO]

  private val r: Resource[IO, CharactersRoutes[IO]] =
    for {
      repoChar <- Resource.eval(CharactersInMemoryRepository[IO])
      repoMovs <- Resource.eval(MovementsInMemoryRepository[IO])
    } yield new CharactersRoutes[IO](
      CharactersService(repoChar, kc),
      MovementsService(repoMovs, kc)
    )

  test("/characters should return characters") {
    r.use(
      router =>
        for {
          response <- router.characterRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/characters"))
          respOk <- TestHelper.checkF[List[KuroganeCharacter]](response, Status.Ok, Some(TestHelper.characters))
        } yield assertEquals(respOk, true)
    )
  }

  test("/characters/joker/moves should return joker moves") {
    r.use(
      router =>
        for {
          response <- router
            .characterRoutes
            .orNotFound
            .run(Request[IO](method = Method.GET, uri = uri"/characters/joker/moves"))
          respOk <- TestHelper.checkF[List[KuroganeCharacterMove]](response, Status.Ok, Some(TestHelper.movements))(modifiedEntityDecoderForMoves)
        } yield assertEquals(respOk, true)
    )
  }

  test("/characters/joker/punishable-moves should return joker punishable moves") {

    r.use(
      router =>
        for {
          response <- router
            .characterRoutes
            .orNotFound
            .run(Request[IO](method = Method.GET, uri = uri"/characters/joker/punishable-moves"))
          respOk <- TestHelper.checkF[List[KuroganeCharacterMove]](response, Status.Ok, Some(TestHelper.movements.take(1)))(modifiedEntityDecoderForMoves)
        } yield assertEquals(respOk, true)
    )
  }

  test("/characters/punishable-moves/42083468d7124245b6b7f58658bb4843/punished-by/joker should return joker moves that can punish move 42083468d7124245b6b7f58658bb4843") {
    r.use(
      router =>
        for {
          response <- router
            .characterRoutes
            .orNotFound
            .run(Request[IO](method = Method.GET, uri = uri"/characters/punishable-moves/42083468d7124245b6b7f58658bb4843/punished-by/joker"))
          respOk <- TestHelper.checkF[List[KuroganeCharacterMove]](response, Status.Ok, Some(TestHelper.movements))(modifiedEntityDecoderForMoves)
        } yield assertEquals(respOk, true)
    )
  }

  test("/characters/punishable-moves/nonexistent-move/punished-by/joker should return 404 not found") {

    r.use(
      router =>
        for {
          response <- router
            .characterRoutes
            .orNotFound
            .run(Request[IO](method = Method.GET, uri = uri"/characters/punishable-moves/nonexistent-move/punished-by/joker"))
          respOk <- TestHelper.checkF(response, Status.NotFound, None)(modifiedEntityDecoderForMoves)
        } yield assertEquals(respOk, true)
    )
  }
  test("/characters/punishable-moves/7a25432add0345549df19a577243a983/punished-by/joker should return Bad Request") {
    r.use(
      router =>
        for {
          response <- router
            .characterRoutes
            .orNotFound
            .run(Request[IO](method = Method.GET, uri = uri"/characters/punishable-moves/7a25432add0345549df19a577243a983/punished-by/joker"))
          respOk <- TestHelper.checkF(response, Status.BadRequest, Some("not.punishable.move"))
        } yield assertEquals(respOk, true)
    )
  }
}
