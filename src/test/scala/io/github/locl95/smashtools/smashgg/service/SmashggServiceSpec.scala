package io.github.locl95.smashtools.smashgg.service

import cats.effect.{IO, Resource}
import io.github.locl95.smashtools.smashgg.{SmashggClient, TestHelper, TournamentsInMemoryRepository}
import munit.CatsEffectSuite
import org.http4s.client.blaze.BlazeClientBuilder
import cats.implicits._

import scala.concurrent.ExecutionContext.global

class SmashggServiceSpec extends CatsEffectSuite{

  val client: Resource[IO, SmashggClient[IO]] = for {
    client <- BlazeClientBuilder[IO](global).resource
    smashggClient = SmashggClient.impl[IO](client)
  } yield smashggClient

  test("Retrieve tournaments from api and insert and get them in data"){
    client.use(client => {
      val tournamentsService: Resource[IO, TournamentService[IO]] =
        for {
          charRepo <- Resource.eval(TournamentsInMemoryRepository[IO])
        } yield new TournamentService[IO](charRepo, client)

      tournamentsService.use(service =>{

        val program = for {
          idTournament <- service.insert(TestHelper.tournament.name)
          tournaments <- service.get
        } yield (idTournament, tournaments)

        assertIO(program._1F, TestHelper.tournament.id)
        assertIO(program._2F, TestHelper.tournaments)
      })
    })
  }
}
