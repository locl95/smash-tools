package io.github.locl95.smashtools.characters.service

import cats.effect.IO
import cats.implicits._
import io.github.locl95.smashtools.characters.{
  CharactersInMemoryRepository,
  KuroganeClientMock,
  MovementsInMemoryRepository,
  TestHelper
}
import munit.CatsEffectSuite

class CharactersServiceSpec extends CatsEffectSuite {

  val kuroganeClient = new KuroganeClientMock[IO]
  test("Get Characters should retrieve them from api and insert them in database when cache is false") {
    val repository = new CharactersInMemoryRepository[IO]
    val program = for {
      apiCharacters <- CharactersService(repository, kuroganeClient).get
      dbCharacters <- repository.get
    } yield (apiCharacters, dbCharacters)
    assertIO(program._1F, TestHelper.characters)
    assertIO(program._2F, TestHelper.characters)

  }

  test("Get Movements should retrieve them from api and insert them in database when cache is false") {
    val repository = new MovementsInMemoryRepository[IO]
    val program = for {
      apiMovements <- MovementsService(repository, kuroganeClient).getMoves("joker")
      dbMovements <- repository.getMoves("joker")
    } yield (apiMovements, dbMovements)

    assertIO(program._1F, TestHelper.movements)
    assertIO(program._2F, TestHelper.movements)
  }
}
