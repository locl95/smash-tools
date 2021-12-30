package io.github.locl95.smashtools.characters.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.characters.TestHelper
import io.github.locl95.smashtools.characters.service.CharactersInMemoryRepository
import munit.CatsEffectSuite

class CharactersRepositorySpec extends CatsEffectSuite {
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  override def beforeEach(context: BeforeEach): Unit = {
    val migrate = for {
      dbProgram <- ctx.databaseProgram
      _ = dbProgram.flyway.clean()
      _ = dbProgram.flyway.migrate()
    } yield ()
    migrate.unsafeRunSync()
  }

  test("Given some characters I can insert them") {
    val insertTest = (repo: CharactersRepository[IO]) =>
      assertIOBoolean(for {
        result <- repo.insert(TestHelper.characters)
      } yield result == 2)
    List(
      new CharactersInMemoryRepository[IO],
      new CharacterPostgresRepository[IO](tx)
    ).foreach(insertTest)
  }

  test("Given i have inserted some characters I can retrieve them") {
    val getTest = (repo: CharactersRepository[IO]) =>
      assertIOBoolean(for {
        _ <- repo.insert(TestHelper.characters)
        result <- repo.get
      } yield result == TestHelper.characters)
    List(
      new CharactersInMemoryRepository[IO],
      new CharacterPostgresRepository[IO](tx)
    ).foreach(getTest)
  }
}
