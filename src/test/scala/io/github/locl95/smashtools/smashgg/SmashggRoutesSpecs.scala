package io.github.locl95.smashtools.smashgg

import cats.effect._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.domain.{Entrant, Event, Phase, Sets, Tournament}
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}

class SmashggRoutesSpecs extends CatsEffectSuite{

  implicit private val entityDecoderForInt: EntityDecoder[IO, Int] = jsonOf

  val ctx = new Context[IO]

  override def beforeAll(): Unit = {
    val migrate = for {
      dbProgram <- ctx.databaseProgram
      _ = dbProgram.flyway.clean()
      _ = dbProgram.flyway.migrate()
    } yield ()
    migrate.unsafeRunSync()
  }

  test("POST /tournaments/<tournament> should insert a tournament into tournaments migration") {
    implicit def entityDecoderForTournaments: EntityDecoder[IO, Tournament] = jsonOf

    ctx.smashggRoutesProgram.use { router =>
      for {
        responseInsert <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments").withEntity(TestHelper.tournament.name))
        responseGet <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/tournaments" / TestHelper.tournament.id.toString))
        responseOkInsert <- TestHelper.checkF[Int](responseInsert, Status.Ok, Some(TestHelper.tournament.id))
        responseOkGet <- TestHelper.checkF[Tournament](responseGet, Status.Ok, Some(TestHelper.tournament))
      } yield {
        assertEquals(responseOkInsert, true)
        assertEquals(responseOkGet, true)
      }
    }

  }

  test("GET /tournaments/312932 should return MST-4 tournament from tournaments migration") {
    implicit  val entityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf

    ctx.smashggRoutesProgram.use { router =>
      for {
        resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/tournaments/312932"))
        respOk <- TestHelper.checkF[Tournament](resp, Status.Ok, Some(TestHelper.tournament))
      } yield assertEquals(respOk, true)
    }

  }

  test("GET /tournaments should return tournaments from tournaments migration") {
    implicit  val entityDecoderForTournaments: EntityDecoder[IO, List[Tournament]] =
      jsonOf

    ctx.smashggRoutesProgram.use { router =>
      for {
        resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/tournaments"))
        respOk <- TestHelper.checkF[List[Tournament]](resp, Status.Ok, Some(List(TestHelper.tournament)))
      } yield assertEquals(respOk, true)
    }
  }

  test("POST /tournaments/<tournament>/events/<event> should insert an event to events migration"){
    implicit val entityEncoderForEvent: EntityEncoder[IO, Event] = jsonEncoderOf

    ctx.smashggRoutesProgram.use {
      router =>
        for {
          resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments" / "312932" / "events" ).withEntity(TestHelper.testEvent))
          respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(615463))
        } yield assertEquals(respOk, true)
    }
  }

  test("GET /tournaments/<tournament>/events should return all events from <tournament> from events migration"){
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event]
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf

    ctx.smashggRoutesProgram.use{
      router =>
        for {
          resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/tournaments" /"312932" /"events"))
          respOk <- TestHelper.checkF[List[Event]](resp, Status.Ok, Some(TestHelper.events))
        } yield assertEquals(respOk, true)
    }

  }

  test("GET /events should return all events from events migration"){
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event]
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf

    ctx.smashggRoutesProgram.use{
      router =>
        for {
          resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/events"))
          respOk <- TestHelper.checkF[List[Event]](resp, Status.Ok, Some(TestHelper.events))
        } yield assertEquals(respOk, true)
    }
  }

  test("POST /events/entrants should insert a list of entrants to entrants migration"){
    implicit val encoderForEntrants: Encoder[List[Entrant]] = Encoder.encodeList[Entrant]
    implicit def entityEncoderEntrants: EntityEncoder[IO, List[Entrant]] = jsonEncoderOf

    ctx.smashggRoutesProgram.use {
      router =>
        for {
          resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/events/entrants").withEntity(TestHelper.entrants))
          respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(2))
        } yield assertEquals(respOk, true)
    }

  }

  test("GET /events/entrants should retrieve all entrants from entrants migration"){
    implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant]
    implicit val entityDecoderForEntrants: EntityDecoder[IO, List[Entrant]] =
      jsonOf

    ctx.smashggRoutesProgram.use{router =>
      for {
        resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/events/entrants"))
        respOk <- TestHelper.checkF[List[Entrant]](resp, Status.Ok, Some(TestHelper.entrants))
      } yield assertEquals(respOk, true)
    }

  }

  test("GET /events/<eventID>/entrants should retrieve all entrants that belong to <eventID> from entrants migration"){
    implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant]
    implicit val entityDecoderForEntrants: EntityDecoder[IO, List[Entrant]] =
      jsonOf

    ctx.smashggRoutesProgram.use{router =>
      for {
        resp <- router.smashggRoutes.orNotFound
          .run(Request[IO](method = Method.GET, uri = uri"/events" /"615463" /"entrants"))
        respOk <- TestHelper.checkF[List[Entrant]](resp, Status.Ok, Some(TestHelper.entrants))
      } yield assertEquals(respOk, true)
    }

  }

  test("POST /phases should insert phases to phases migration"){
    implicit def entityEncoderForSets: EntityEncoder[IO, List[Phase]] = jsonEncoderOf

    ctx.smashggRoutesProgram.use{router =>
      for {
        resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/phases").withEntity(TestHelper.phases))
        respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(2))
      } yield assertEquals(respOk, true)
    }

  }

  test("GET /phases should retrieve all phases from phases migration"){
    implicit val entityDecoderForPhases: EntityDecoder[IO, List[Phase]] = jsonOf

    ctx.smashggRoutesProgram.use{router =>
      for {
        resp <- router.smashggRoutes.orNotFound
          .run(Request[IO](method = Method.GET, uri = uri"/phases"))
        respOk <- TestHelper.checkF[List[Phase]](resp, Status.Ok, Some(TestHelper.phases))
      } yield assertEquals(respOk, true)
    }

  }

  test("POST /sets should insert sets to sets migration"){
    implicit def entityEncoderForSets: EntityEncoder[IO, List[Sets]] = jsonEncoderOf

    ctx.smashggRoutesProgram.use(router =>
      for {
        resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/sets").withEntity(TestHelper.sets))
        respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(2))
      } yield assertEquals(respOk, true)
    )
  }

  test("GET /sets should retrieve all sets from sets migration"){
    implicit def entityDecoderForSets: EntityDecoder[IO, List[Sets]] = jsonOf

    ctx.smashggRoutesProgram.use(router =>
      for {
        resp <- router.smashggRoutes.orNotFound
          .run(Request[IO](method = Method.GET, uri = uri"/sets"))
        respOk <- TestHelper.checkF[List[Sets]](resp, Status.Ok, Some(TestHelper.testSets))
      } yield assertEquals(respOk, true)
    )
  }

}
