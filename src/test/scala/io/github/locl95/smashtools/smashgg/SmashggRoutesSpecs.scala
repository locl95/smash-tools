package io.github.locl95.smashtools.smashgg

//import cats.effect.IO
//import io.github.locl95.smashtools.smashgg.domain.Tournament
//import io.github.locl95.smashtools.smashgg.service.TournamentService
//import munit.CatsEffectSuite
//import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
//import org.http4s.{Method, Request, Status}
//import io.github.locl95.smashtools.smashgg.protocol.Smashgg.tournamentsEntityDecoder
//
//class SmashggRoutesSpecs extends CatsEffectSuite{
//
//  private val smashggClient: SmashggClient[IO] = new SmashggClientMock[IO]
//
//  private val router = new SmashggRoutes[IO](
//    TournamentService(new TournamentsInMemoryRepository[IO], smashggClient)
//  ).smashggRoutes
//
//  test("/tournaments should return tournaments") {
//    val response = router
//      .orNotFound
//      .run(Request[IO](method = Method.GET, uri = uri"/tournaments"))
//    assertEquals(TestHelper.check[List[Tournament]](response, Status.Ok, Some(TestHelper.tournaments)), true)
//  }
//
//}
