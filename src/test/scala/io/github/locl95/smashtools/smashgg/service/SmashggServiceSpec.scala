package io.github.locl95.smashtools.smashgg.service

import cats.effect.IO
import cats.implicits.catsSyntaxFunctorTuple2Ops
import io.github.locl95.smashtools.smashgg.{SmashggClient, TestHelper, TournamentsInMemoryRepository}
import munit.CatsEffectSuite
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

class SmashggServiceSpec extends CatsEffectSuite{

  val program: fs2.Stream[IO, Client[IO]] = for {
    client <- BlazeClientBuilder[IO](global).stream
  } yield client

  val client: Client[IO] = program.compile.lastOrError.unsafeRunSync()
  val smashggClient: SmashggClient[IO] = SmashggClient.impl(client)

  test("Retrieve tournaments from api and insert and get them in data"){
    val repository = new TournamentsInMemoryRepository[IO]

    val program = for {
      apiTournaments <- {
        for {
          _ <- TournamentService(repository, smashggClient).insert(TestHelper.tournament)
          tournaments  <- TournamentService(repository, smashggClient).get
        } yield tournaments
      }
      dbTournaments <- repository.get
    } yield (apiTournaments, dbTournaments)

    assertIO(program._1F, TestHelper.tournaments)
    assertIO(program._2F, TestHelper.tournaments)
  }
}
