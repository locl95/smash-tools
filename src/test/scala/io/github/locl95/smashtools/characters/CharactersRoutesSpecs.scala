package io.github.locl95.smashtools.characters

import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import munit.CatsEffectSuite
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{EntityDecoder, HttpRoutes, Method, Request, Response, Status}
import io.github.locl95.smashtools.characters.protocol.Kurogane.kuroganeCharactersEntityDecoder
import org.http4s.circe.jsonOf

class CharactersRoutesSpecs extends CatsEffectSuite {

  private val modifiedDecoderForMove: Decoder[KuroganeCharacterMove] =
    deriveDecoder[KuroganeCharacterMove]

  implicit private val modifiedDecoderForMoves: Decoder[List[KuroganeCharacterMove]] =
    Decoder.decodeList[KuroganeCharacterMove](modifiedDecoderForMove)
  private val modifiedEntityDecoderForMoves: EntityDecoder[IO, List[KuroganeCharacterMove]] = jsonOf
  private val kc: KuroganeClient[IO] = new KuroganeClientMock[IO]

  private val router: HttpRoutes[IO] = new CharactersRoutes[IO](
    CharactersService(new CharactersInMemoryRepository[IO], kc),
    MovementsService(new MovementsInMemoryRepository[IO], kc)
  ).characterRoutes

  test("/characters should return characters") {
    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/characters"))
    assertEquals(TestHelper.check[List[KuroganeCharacter]](response, Status.Ok, Some(TestHelper.characters)), true)
  }

  test("/characters/joker/moves should return joker moves") {
    val response = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/characters/joker/moves"))
    assertEquals(
      TestHelper.check[List[KuroganeCharacterMove]](response, Status.Ok, Some(TestHelper.movements))(
        modifiedEntityDecoderForMoves
      ),
      true
    )
  }
  test("/characters/joker/punishable-moves should return joker punishable moves") {
    val response = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/characters/joker/punishable-moves"))
    assertEquals(
      TestHelper.check[List[KuroganeCharacterMove]](response, Status.Ok, Some(TestHelper.movements.take(1)))(
        modifiedEntityDecoderForMoves
      ),
      true
    )
  }
  test(
    "/characters/punishable-moves/42083468d7124245b6b7f58658bb4843/punished-by/joker should return joker moves that can punish move 42083468d7124245b6b7f58658bb4843"
  ) {
    val response = router
      .orNotFound
      .run(
        Request[IO](
          method = Method.GET,
          uri = uri"/characters/punishable-moves/42083468d7124245b6b7f58658bb4843/punished-by/joker"
        )
      )
    assertEquals(
      TestHelper.check[List[KuroganeCharacterMove]](response, Status.Ok, Some(TestHelper.movements))(
        modifiedEntityDecoderForMoves
      ),
      true
    )
  }
  test(
    "/characters/punishable-moves/nonexistent-move/punished-by/joker should return 404 not found"
  ) {
    val response = router
      .orNotFound
      .run(
        Request[IO](
          method = Method.GET,
          uri = uri"/characters/punishable-moves/nonexistent-move/punished-by/joker"
        )
      )
    assertEquals(
      TestHelper.check(response, Status.NotFound, None)(
        modifiedEntityDecoderForMoves
      ),
      true
    )
  }
  test(
    "/characters/punishable-moves/7a25432add0345549df19a577243a983/punished-by/joker should return Bad Request"
  ) {
    val response = router
      .orNotFound
      .run(
        Request[IO](
          method = Method.GET,
          uri = uri"/characters/punishable-moves/7a25432add0345549df19a577243a983/punished-by/joker"
        )
      )
    assertEquals(
      TestHelper.check(response, Status.BadRequest, Some("not.punishable.move")),
      true
    )
  }
}
