package io.github.locl95.smashtools.characters.service

import cats.effect.IO
import cats.implicits._
import io.github.locl95.smashtools.characters.{
  CharactersInMemoryRepository,
  KuroganeClient,
  MovementsInMemoryRepository,
  TestHelper
}
import munit.CatsEffectSuite
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global

class CharactersServiceSpec extends CatsEffectSuite {
  test("Get Characters should retrieve them from api and insert them in database when cache is false") {
    val repository = new CharactersInMemoryRepository[IO]
    val program = for {
      client <- BlazeClientBuilder[IO](global).stream
      kuroganeClient = KuroganeClient.impl(client)
      apiCharacters <- fs2.Stream.eval(CharactersService(repository, kuroganeClient).get)
      dbCharacters <- fs2.Stream.eval(repository.get)
    } yield (apiCharacters, dbCharacters)
    assertIO(
      program.compile.foldMonoid.map(_._1.take(2)),
      TestHelper.characters
    )
    assertIO(
      program.compile.foldMonoid.map(_._2.take(2)),
      TestHelper.characters
    )

  }

  test("Get Movements should retrieve them from api and insert them in database when cache is false") {
    val repository = new MovementsInMemoryRepository[IO]
    val program = for {
      client <- BlazeClientBuilder[IO](global).stream
      kuroganeClient = KuroganeClient.impl(client)
      apiMovements <- fs2.Stream.eval(MovementsService(repository, kuroganeClient).get("joker"))
      dbMovements <- fs2.Stream.eval(repository.get("joker"))
    } yield (apiMovements, dbMovements)

    assertIO(
      program.compile.foldMonoid.map(_._1.take(2)),
      TestHelper.movements
    )
    assertIO(
      program.compile.foldMonoid.map(_._2.take(2)),
      TestHelper.movements
    )
  }
}
