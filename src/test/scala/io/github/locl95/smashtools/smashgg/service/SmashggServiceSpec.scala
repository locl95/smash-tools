package io.github.locl95.smashtools.smashgg.service

//import cats.effect.IO
//import cats.implicits.catsSyntaxFunctorTuple2Ops
//import io.github.locl95.smashtools.smashgg.{SmashggClientMock, TestHelper, TournamentsInMemoryRepository}
//import munit.CatsEffectSuite
//
//class SmashggServiceSpec extends CatsEffectSuite{
//
//  val smashggClient = new SmashggClientMock[IO]
//
//  test("Get should retrieve tournaments from api and insert them in data"){
//    val repository = new TournamentsInMemoryRepository[IO]
//    val program = for {
//      apiTournaments <- {
//        for {
//          _ <- TournamentService(repository, smashggClient).insert(TestHelper.tournament)
//          tournaments  <- TournamentService(repository, smashggClient).get
//        } yield tournaments
//      }
//      dbTournaments <- repository.get
//    } yield (apiTournaments, dbTournaments)
//
//    assertIO(program._1F, TestHelper.tournaments)
//    assertIO(program._2F, TestHelper.tournaments)
//  }
//}
