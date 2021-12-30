package io.github.locl95.smashtools.smashgg

import cats.effect.IO
import io.github.locl95.smashtools.smashgg.domain.{Event, Participant, SmashggQuery, Tournament}
import io.github.locl95.smashtools.smashgg.protocol.Smashgg._
import munit.CatsEffectSuite
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

class SmashggClientSpec extends CatsEffectSuite {
  test("Should be able to retreat a tournament from smash.gg API") {
    val program: fs2.Stream[IO, Tournament] = for {
      client <- BlazeClientBuilder[IO](global).stream
      smashggClient = SmashggClient.impl(client)
      tournament <- fs2
        .Stream
        .eval(smashggClient.get[Tournament](SmashggQuery.getTournamentQuery("mst-4")))
    } yield tournament

    assertIO(program.compile.lastOrError, Tournament("MST 4"))
  }

  test("Should be able to retreat participants from smash.gg API") {
    val program: fs2.Stream[IO, List[Participant]] = for {
      client <- BlazeClientBuilder[IO](global).stream
      smashggClient = SmashggClient.impl(client)
      participants <- fs2
        .Stream
        .eval(smashggClient.get[List[Participant]](SmashggQuery.getParticipantsQuery("mst-4", 1)))
    } yield participants

    assertIO(program.compile.foldMonoid.map(i => i.contains(Participant(List(7919272)))), true)
  }

  test("Should be able to retreat events from smash.gg API") {
    val program: fs2.Stream[IO, Event] = for {
      client <- BlazeClientBuilder[IO](global).stream
      smashggClient = SmashggClient.impl(client)
      participants <- fs2
        .Stream
        .eval(smashggClient.get[Event](SmashggQuery.getEvent("mst-4", "ultimate-singles", 1)))
    } yield participants

    assertIO(program.compile.lastOrError, Event("Ultimate Singles"))
  }
}
