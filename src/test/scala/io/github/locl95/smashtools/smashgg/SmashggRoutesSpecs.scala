package io.github.locl95.smashtools.smashgg

import cats.effect._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, HCursor}
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.domain.{SmashggQuery, Tournament}
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}

import scala.concurrent.ExecutionContext.global

class SmashggRoutesSpecs extends CatsEffectSuite{

  implicit private val smashggQueryEncoder: Encoder[SmashggQuery] = deriveEncoder[SmashggQuery]
  implicit private def smashggQueryEntityEncoder[F[_]]: EntityEncoder[F, SmashggQuery] = jsonEncoderOf


  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor
  val smashggRouter: SmashggRoutes[IO] = ctx.smashggRoutesProgram.compile.lastOrError.unsafeRunSync()
  val router: HttpRoutes[IO] = smashggRouter.smashggRoutes

  test("POST /tournaments/<tournament> should insert the tournament info from smashgg API"){
    implicit val modifiedDecoderForTournament: Decoder[Tournament] = (c: HCursor) => {
      for {
        id <- c.downField("data").downField("tournament").downField("id").as[Int]
        name <- c.downField("data").downField("tournament").downField("name").as[String]
      } yield Tournament(id,name.map(x => if(x.toString.equals(" ")) '-' else x))
    }
    implicit  val modifiedEntityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf
    implicit  val encoderForTournament: Encoder[Tournament] = deriveEncoder[Tournament]
    implicit val entityDecoderForInt: EntityDecoder[IO, Int] = jsonOf

    val program: fs2.Stream[IO, Tournament] = for {
      client <- BlazeClientBuilder[IO](global).stream
      smashggClient = SmashggClient.impl(client)
      tournament <- fs2
        .Stream
        .eval(smashggClient.get[Tournament](SmashggQuery.getTournamentQuery("mst-4")))
    } yield tournament

    val response = for {
      t <- program.compile.lastOrError
      resp <- router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments").withEntity(t))
    } yield resp

    assertEquals(TestHelper.check[Int](response, Status.Ok, Some(1)), true)
  }

  test("GET /tournaments/MST-4 should return MST-4 tournament info from Smashgg Routes") {

    implicit  val modifiedDecoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
    implicit  val modifiedEntityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments/MST-4"))

    assertEquals(TestHelper.check[Tournament](response, Status.Ok, Some(TestHelper.tournament)), true)
  }

  test("GET /tournaments should return tournaments from Smashgg Routes") {
    implicit  val modifiedDecoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
    implicit  val modifiedDecoderForTournaments: Decoder[List[Tournament]] =
      Decoder.decodeList[Tournament](modifiedDecoderForTournament)

    implicit  val modifiedEntityDecoderForTournaments: EntityDecoder[IO, List[Tournament]] =
      jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments"))

    assertEquals(TestHelper.check[List[Tournament]](response, Status.Ok, Some(TestHelper.tournaments)), true)
  }

}
