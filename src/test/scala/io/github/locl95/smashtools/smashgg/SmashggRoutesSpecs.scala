package io.github.locl95.smashtools.smashgg

import cats.effect._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.domain.{Event, SmashggQuery, Tournament}
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
  implicit val entityDecoderForInt: EntityDecoder[IO, Int] = jsonOf

  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor
  val smashggRouter: SmashggRoutes[IO] = ctx.smashggRoutesProgram.compile.lastOrError.unsafeRunSync()
  val router: HttpRoutes[IO] = smashggRouter.smashggRoutes

  test("POST /tournaments/<tournament> should insert the tournament info from smashgg API"){

    import io.github.locl95.smashtools.smashgg.protocol.Smashgg.tournamentDecoder
    implicit val entityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf
    implicit val encoderForTournament: Encoder[Tournament] = deriveEncoder[Tournament]


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

    implicit  val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
    implicit  val entityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments/MST-4"))

    assertEquals(TestHelper.check[Tournament](response, Status.Ok, Some(TestHelper.tournament)), true)
  }

  test("GET /tournaments should return tournaments from Smashgg Routes") {
    implicit  val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
    implicit  val decoderForTournaments: Decoder[List[Tournament]] =
      Decoder.decodeList[Tournament](decoderForTournament)
    implicit  val entityDecoderForTournaments: EntityDecoder[IO, List[Tournament]] =
      jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments"))

    assertEquals(TestHelper.check[List[Tournament]](response, Status.Ok, Some(TestHelper.tournaments)), true)
  }

  test("POST /tournaments/<tournament>/events/<event> should insert an event to events"){

    import io.github.locl95.smashtools.smashgg.protocol.Smashgg.eventDecoder
    implicit val entityDecoder: EntityDecoder[IO, Event] = jsonOf
    implicit val encoderForEvent: Encoder[Event] = deriveEncoder[Event]

    val program: fs2.Stream[IO, Event] = for {
      client <- BlazeClientBuilder[IO](global).stream
      smashggClient = SmashggClient.impl(client)
      event <- fs2
        .Stream
        .eval(smashggClient.get[Event](SmashggQuery.getEvent("mst-4", "ultimate-singles", 1)))
    } yield event

    val response = for {
      e <- program.compile.lastOrError
      resp <- router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments" / "312932" / "events" ).withEntity(e))
    } yield resp

    assertEquals(TestHelper.check[Int](response, Status.Ok, Some(1)), true)
  }

  test("GET /tournaments/<tournament>/events should return all events from <tournament>"){
    implicit val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event](decoderForEvent)
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments" /"312932" /"events"))

    assertEquals(TestHelper.check[List[Event]](response, Status.Ok, Some(TestHelper.events)), true)
  }

  test("GET /events should return all events"){
    implicit val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event](decoderForEvent)
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/events"))

    assertEquals(TestHelper.check[List[Event]](response, Status.Ok, Some(TestHelper.events)), true)
  }

}
