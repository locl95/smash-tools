package io.github.locl95.smashtools.smashgg

import cats.effect._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveDecoder
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.domain._
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}

class SmashggRoutesSpecs extends CatsEffectSuite{

  implicit private val entityDecoderForInt: EntityDecoder[IO, Int] = jsonOf

  val ctx = new Context[IO]
  val smashggRouter: SmashggRoutes[IO] = ctx.smashggRoutesProgram.compile.lastOrError.unsafeRunSync()
  val router: HttpRoutes[IO] = smashggRouter.smashggRoutes

  override def beforeAll(): Unit = {
    val migrate = for {
      dbProgram <- ctx.databaseProgram
      _ = dbProgram.flyway.clean()
      _ = dbProgram.flyway.migrate()
    } yield ()
    migrate.unsafeRunSync()
  }

  test("POST /tournaments/<tournament> should insert a tournament into tournaments migration"){
    import io.github.locl95.smashtools.smashgg.protocol.Smashgg.tournamentEncoder
    implicit  val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
    implicit  val entityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf

    val responseInsert =
      router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments", headers = TestHelper.headers).withEntity(TestHelper.tournament))

    val responseGet =
      router.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/tournaments" / TestHelper.tournament.id.toString))

    assertEquals(TestHelper.check[Int](responseInsert, Status.Ok, Some(TestHelper.tournament.id)), true)
    assertEquals(TestHelper.check[Tournament](responseGet, Status.Ok, Some(TestHelper.tournament)), true)
  }

  test("GET /tournaments/312932 should return MST-4 tournament from tournaments migration") {
    implicit  val decoderForTournament: Decoder[Tournament] = deriveDecoder[Tournament]
    implicit  val entityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf

    val response = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments/312932"))

    assertEquals(TestHelper.check[Tournament](response, Status.Ok, Some(TestHelper.tournament)), true)
  }

  test("GET /tournaments should return tournaments from tournaments migration") {
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

  test("POST /tournaments/<tournament>/events/<event> should insert an event to events migration"){
    val resp =
      router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments" / "312932" / "events" ).withEntity(TestHelper.testEvent))

    assertEquals(TestHelper.check[Int](resp, Status.Ok, Some(615463)), true)
  }

  test("GET /tournaments/<tournament>/events should return all events from <tournament> from events migration"){
    implicit val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event](decoderForEvent)
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/tournaments" /"312932" /"events"))

    assertEquals(TestHelper.check[List[Event]](response, Status.Ok, Some(TestHelper.events)), true)
  }

  test("GET /events should return all events from events migration"){
    implicit val decoderForEvent: Decoder[Event] = deriveDecoder[Event]
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event](decoderForEvent)
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/events"))

    assertEquals(TestHelper.check[List[Event]](response, Status.Ok, Some(TestHelper.events)), true)
  }

  test("POST /events/entrants should insert a list of entrants to entrants migration"){
    val response =
      router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/events/entrants").withEntity(TestHelper.entrants))

    assertEquals(TestHelper.check[Int](response, Status.Ok, Some(2)), true)
  }

  test("GET /events/entrants should retrieve all entrants from entrants migration"){
    implicit val decoderForEntrant: Decoder[Entrant] = deriveDecoder[Entrant]
    implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant](decoderForEntrant)
    implicit val entityDecoderForEntrants: EntityDecoder[IO, List[Entrant]] =
      jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/events/entrants"))

    assert(response.unsafeRunSync().as[List[Entrant]].unsafeRunSync().take(2) == TestHelper.entrants)
  }

  test("GET /events/<eventID>/entrants should retrieve all entrants that belong to <eventID> from entrants migration"){
    implicit val decoderForEntrant: Decoder[Entrant] = deriveDecoder[Entrant]
    implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant](decoderForEntrant)
    implicit val entityDecoderForEntrants: EntityDecoder[IO, List[Entrant]] =
      jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/events" /"615463" /"entrants"))

    assert(response.unsafeRunSync().as[List[Entrant]].unsafeRunSync().contains(TestHelper.entrant))
  }

  test("POST /phases should insert phases to phases migration"){
    val response =
      router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/phases").withEntity(TestHelper.phases))

    assertEquals(TestHelper.check[Int](response, Status.Ok, Some(2)), true)
  }

  test("GET /phases should retrieve all phases from phases migration"){
    implicit val decoderForPhases: Decoder[List[Phase]] = Decoder.decodeList[Phase]
    implicit val entityDecoderForPhases: EntityDecoder[IO, List[Phase]] = jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/phases"))

    assertEquals(TestHelper.check[List[Phase]](response, Status.Ok, Some(TestHelper.phases)), true)
  }

  test("POST /sets should insert sets to sets migration"){
    val response =
      router.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/sets").withEntity(TestHelper.sets))

    assertEquals(TestHelper.check[Int](response, Status.Ok, Some(2)), true)
  }

  test("GET /sets should retrieve all sets from sets migration"){
    implicit val decoderForSets: Decoder[List[Sets]] = Decoder.decodeList[Sets]
    implicit def entityDecoderForSets: EntityDecoder[IO, List[Sets]] = jsonOf

    val response: IO[Response[IO]] = router
      .orNotFound
      .run(Request[IO](method = Method.GET, uri = uri"/sets"))

    assert(response.unsafeRunSync().as[List[Sets]].unsafeRunSync().take(2) == TestHelper.testSets)
  }
}
