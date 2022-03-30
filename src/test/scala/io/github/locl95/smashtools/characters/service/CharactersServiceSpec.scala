package io.github.locl95.smashtools.characters.service

import cats.effect.{IO, Resource}
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

  val charactersService: Resource[IO, CharactersService[IO]] =
    for {
      charRepo <- Resource.eval(CharactersInMemoryRepository[IO])
    } yield new CharactersService[IO](charRepo, kuroganeClient)

  val movementsService: Resource[IO, MovementsService[IO]] =
    for {
      movsRepo <- Resource.eval(MovementsInMemoryRepository[IO])
    } yield new MovementsService[IO](movsRepo, kuroganeClient)

  test("Get Characters should retrieve them from api and insert them in database when cache is false") {
    charactersService.use(
      service => {
        val apiDbCharacters =
          for {
            apiCharacters <- service.get
            dbCharacters <- service.get
          } yield (apiCharacters, dbCharacters)
        assertIO(apiDbCharacters._1F, TestHelper.characters)
        assertIO(apiDbCharacters._2F, TestHelper.characters)
      }
    )
  }

  test("Get Movements should retrieve them from api and insert them in database when cache is false") {
    movementsService.use(
      service => {
        val apiDbMovements =
          for {
            apiMovements <- service.getMoves("joker")
            dbMovements <- service.getMoves("joker")
          } yield (apiMovements, dbMovements)
        assertIO(apiDbMovements._1F, TestHelper.movements)
        assertIO(apiDbMovements._2F, TestHelper.movements)
      }
    )
  }
}
