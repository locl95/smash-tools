package io.github.locl95.smashtools.smashgg

import cats.effect._
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import io.github.locl95.smashtools.smashgg.domain._
import io.github.locl95.smashtools.{Context, JdbcDatabaseConfiguration, SmashggAuth}
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.server.AuthMiddleware

class SmashggRoutesSpecs extends CatsEffectSuite{

  implicit private val entityDecoderForInt: EntityDecoder[IO, Int] = jsonOf

  val context: Resource[IO, Context[IO]] = Context.test[IO](JdbcDatabaseConfiguration("org.postgresql.Driver", "jdbc:postgresql:smashtools", "test", "test", 5, 10))

  val users = new UsersInMemoryRepository[IO]
  users.insert(User(1212))

  val credentials = new CredentialsInMemoryRepository[IO]
  credentials.insert(Credential(1212, "3305177ceda157c60fbc09b79e2ff987"))

  val authMiddleware: AuthMiddleware[IO, User] = SmashggAuth.make[IO](users, credentials).middleware

  test("Authed POST /tournaments/<tournament> should insert a tournament into tournaments migration") {
    context.use{
      ctx => ctx.smashggRoutesProgram.use { router =>
        for {
          responseInsert <-
            authMiddleware(router.authedSmashhggRoutes).orNotFound
              .run(Request[IO](method = Method.POST, uri = uri"/tournaments", headers = TestHelper.headers).withEntity(TestHelper.tournament.name))

          responseOkInsert <- TestHelper.checkF[Int](responseInsert, Status.Ok, Some(TestHelper.tournament.id))
        } yield
          assertEquals(responseOkInsert, true)
      }
    }
  }

  test("GET /tournaments/312932 should return MST-4 tournament from tournaments migration") {
    implicit  val entityDecoderForTournament: EntityDecoder[IO, Tournament] =
      jsonOf

    context.use{
      ctx =>
        ctx.smashggRoutesProgram.use { router =>
        for {
          _ <- authMiddleware(router.authedSmashhggRoutes).orNotFound
              .run(Request[IO](method = Method.POST, uri = uri"/tournaments", headers = TestHelper.headers).withEntity(TestHelper.tournament.name))
          resp <- router.smashggRoutes.orNotFound
            .run(Request[IO](method = Method.GET, uri = uri"/tournaments/312932"))
          respOk <- TestHelper.checkF[Tournament](resp, Status.Ok, Some(TestHelper.tournament))
        } yield assertEquals(respOk, true)
      }
    }
  }

  test("GET /tournaments should return tournaments from tournaments migration") {
    implicit  val entityDecoderForTournaments: EntityDecoder[IO, List[Tournament]] =
      jsonOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use { router =>
          for {
            _ <- authMiddleware(router.authedSmashhggRoutes).orNotFound
              .run(Request[IO](method = Method.POST, uri = uri"/tournaments", headers = TestHelper.headers).withEntity(TestHelper.tournament.name))
            resp <- router.smashggRoutes.orNotFound
              .run(Request[IO](method = Method.GET, uri = uri"/tournaments"))
            respOk <- TestHelper.checkF[List[Tournament]](resp, Status.Ok, Some(List(TestHelper.tournament)))
          } yield assertEquals(respOk, true)
        }
    }
  }

  test("POST /tournaments/<tournament>/events/<event> should insert an event to events migration"){
    implicit val entityEncoderForEvent: EntityEncoder[IO, Event] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use {
          router =>
            for {
              resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments" / "312932" / "events").withEntity(TestHelper.testEvent))
              respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(615463))
            } yield assertEquals(respOk, true)
        }
    }
  }

  test("GET /tournaments/<tournament>/events should return all events from <tournament> from events migration"){
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event]
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf
    implicit val entityEncoderForEvent: EntityEncoder[IO, Event] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use {
          router =>
            for {
              _ <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments" / "312932" / "events").withEntity(TestHelper.testEvent))
              resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/tournaments" / "312932" / "events"))
              respOk <- TestHelper.checkF[List[Event]](resp, Status.Ok, Some(TestHelper.events))
            } yield assertEquals(respOk, true)
        }
    }
  }

  test("GET /events should return all events from events migration"){
    implicit val decoderForEvents: Decoder[List[Event]] = Decoder.decodeList[Event]
    implicit val entityDecoderForEvents: EntityDecoder[IO, List[Event]] = jsonOf
    implicit val entityEncoderForEvent: EntityEncoder[IO, Event] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use {
          router =>
            for {
              _ <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/tournaments" / "312932" / "events").withEntity(TestHelper.testEvent))
              resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/events"))
              respOk <- TestHelper.checkF[List[Event]](resp, Status.Ok, Some(TestHelper.events))
            } yield assertEquals(respOk, true)
        }
    }
  }

  test("POST /events/entrants should insert a list of entrants to entrants migration"){
    implicit val encoderForEntrants: Encoder[List[Entrant]] = Encoder.encodeList[Entrant]
    implicit def entityEncoderEntrants: EntityEncoder[IO, List[Entrant]] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use {
          router =>
            for {
              resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/events/entrants").withEntity(TestHelper.entrants))
              respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(2))
            } yield assertEquals(respOk, true)
        }
    }
  }

  test("GET /events/entrants should retrieve all entrants from entrants migration"){
    implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant]
    implicit val entityDecoderForEntrants: EntityDecoder[IO, List[Entrant]] =
      jsonOf
    implicit val encoderForEntrants: Encoder[List[Entrant]] = Encoder.encodeList[Entrant]
    implicit def entityEncoderEntrants: EntityEncoder[IO, List[Entrant]] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use { router =>
          for {
            _ <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/events/entrants").withEntity(TestHelper.entrants))
            resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.GET, uri = uri"/events/entrants"))
            respOk <- TestHelper.checkF[List[Entrant]](resp, Status.Ok, Some(TestHelper.entrants))
          } yield assertEquals(respOk, true)
        }
    }
  }

  test("GET /events/<eventID>/entrants should retrieve all entrants that belong to <eventID> from entrants migration"){
    implicit val decoderForEntrants: Decoder[List[Entrant]] = Decoder.decodeList[Entrant]
    implicit val entityDecoderForEntrants: EntityDecoder[IO, List[Entrant]] =
      jsonOf
    implicit val encoderForEntrants: Encoder[List[Entrant]] = Encoder.encodeList[Entrant]
    implicit def entityEncoderEntrants: EntityEncoder[IO, List[Entrant]] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use { router =>
          for {
            _ <- router.smashggRoutes.orNotFound
              .run(Request[IO](method = Method.POST, uri = uri"/events/entrants").withEntity(TestHelper.entrants))
            resp <- router.smashggRoutes.orNotFound
              .run(Request[IO](method = Method.GET, uri = uri"/events" / "615463" / "entrants"))
            respOk <- TestHelper.checkF[List[Entrant]](resp, Status.Ok, Some(TestHelper.entrants))
          } yield assertEquals(respOk, true)
        }
    }
  }

  test("POST /phases should insert phases to phases migration"){
    implicit def entityEncoderForSets: EntityEncoder[IO, List[Phase]] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use { router =>
          for {
            resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/phases").withEntity(TestHelper.phases))
            respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(2))
          } yield assertEquals(respOk, true)
        }
    }
  }

  test("GET /phases should retrieve all phases from phases migration"){
    implicit val entityDecoderForPhases: EntityDecoder[IO, List[Phase]] = jsonOf
    implicit def entityEncoderForSets: EntityEncoder[IO, List[Phase]] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use { router =>
          for {
            _ <- router.smashggRoutes.orNotFound
              .run(Request[IO](method = Method.POST, uri = uri"/phases").withEntity(TestHelper.phases))
            resp <- router.smashggRoutes.orNotFound
              .run(Request[IO](method = Method.GET, uri = uri"/phases"))
            respOk <- TestHelper.checkF[List[Phase]](resp, Status.Ok, Some(TestHelper.phases))
          } yield assertEquals(respOk, true)
        }
    }
  }

  test("POST /sets should insert sets to sets migration"){
    implicit def entityEncoderForSets: EntityEncoder[IO, List[Sets]] = jsonEncoderOf

    context.use{
      ctx =>
        ctx.smashggRoutesProgram.use(router =>
        for {
          resp <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/sets").withEntity(TestHelper.sets))
          respOk <- TestHelper.checkF[Int](resp, Status.Ok, Some(2))
        } yield assertEquals(respOk, true)
    )}
  }

  test("GET /sets should retrieve all sets from sets migration") {
    implicit def entityDecoderForSets: EntityDecoder[IO, List[Sets]] = jsonOf
    implicit def entityEncoderForSets: EntityEncoder[IO, List[Sets]] = jsonEncoderOf

    context.use {
      ctx =>
        ctx.smashggRoutesProgram.use(router =>
          for {
            _ <- router.smashggRoutes.orNotFound.run(Request[IO](method = Method.POST, uri = uri"/sets").withEntity(TestHelper.sets))
            resp <- router.smashggRoutes.orNotFound
              .run(Request[IO](method = Method.GET, uri = uri"/sets"))
            respOk <- TestHelper.checkF[List[Sets]](resp, Status.Ok, Some(TestHelper.testSets))
          } yield assertEquals(respOk, true)
        )
    }
  }
}
