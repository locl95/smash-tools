package io.github.locl95.smashtools.smashgg

import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.domain.Tournament
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}

class SmashggRoutesSpecs extends CatsEffectSuite{

  implicit private val modifiedDecoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
  implicit private val modifiedDecoderForTournaments: Decoder[List[Tournament]] =
    Decoder.decodeList[Tournament](modifiedDecoderForTournament)
  implicit private val modifiedEntityDecoderForTournament: EntityDecoder[IO, Tournament] =
    jsonOf
  implicit private val modifiedEntityDecoderForTournaments: EntityDecoder[IO, List[Tournament]] =
    jsonOf

  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor
  val smashggRouter: SmashggRoutes[IO] = ctx.smashggRoutesProgram.compile.lastOrError.unsafeRunSync()
  val router: HttpRoutes[IO] = smashggRouter.smashggRoutes

  test("/tournaments/MST-4 should return MST-4 tournament info from Smashgg Routes") {

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments/MST-4"))

    assertEquals(TestHelper.check[Tournament](response, Status.Ok, Some(TestHelper.tournament)), true)
  }

  test("/tournaments should return tournaments from Smashgg Routes") {

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments"))

    assertEquals(TestHelper.check[List[Tournament]](response, Status.Ok, Some(TestHelper.tournaments)), true)
  }



}
