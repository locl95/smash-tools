package io.github.locl95.smashtools.smashgg.service

//import cats.effect.IO
//import io.github.locl95.smashtools.smashgg.{TestHelper, TournamentsInMemoryRepository}
//import munit.CatsEffectSuite
//
//class SmashggServiceSpec extends CatsEffectSuite{
//  val smashggClient = new SmashggClientMock[IO]
//  test("Get tournaments should retrieve them from api and insert hem in data"){
//    val repository = new TournamentsInMemoryRepository[IO]
//    val program = for {
//      apiTournaments <- TournamentService(repository, smashggClient).get
//      dbTournamnts <- repository.get
//    } yield (apiTournaments, dbTournamnts)
//
//    assertIO(program._1F, TestHelper.tournament)
//    assertIO(program._2F, TestHelper.tournament)
//  }
//}
