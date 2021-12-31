package io.github.locl95.smashtools.characters.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.characters.{CharactersInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class CharactersRepositorySpec extends CatsEffectSuite {
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new CharactersInMemoryRepository[IO]
  private val postgres = new CharacterPostgresRepository[IO](tx)
  private val repositories = List(inMemory, postgres)

  override def beforeEach(context: BeforeEach): Unit = {
    val migrate = for {
      dbProgram <- ctx.databaseProgram
      _ = dbProgram.flyway.clean()
      _ = dbProgram.flyway.migrate()
    } yield ()
    migrate.unsafeRunSync()
    inMemory.clean()
  }

  private val insertTest = (repo: CharactersRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.characters)
    } yield result == 2)

  private val getTest = (repo: CharactersRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.characters)
      result <- repo.get
    } yield result == TestHelper.characters)

  private val cacheTest = (repo: CharactersRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.cache
      result <- repo.isCached
    } yield result)

  repositories.foreach { r =>
    test(s"Given some characters I can insert them with $r") { insertTest(r) }
    test(s"Given I have inserted some characters I can retrieve them with $r") { getTest(r) }
    test(s"Given I cached a table, isCached should return true with $r") { cacheTest(r) }
  }
}
