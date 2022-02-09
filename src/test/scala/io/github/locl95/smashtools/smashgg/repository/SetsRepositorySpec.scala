package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{SetsInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class SetsRepositorySpec extends CatsEffectSuite {
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new SetsInMemoryRepository[IO]
  private val postgres = new SetsPostgresRepository[IO](tx)
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

  private val insertTest = (repo: SetsRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.testSets)
    } yield result == 2)

  private val getTest = (repo: SetsRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.testSets)
      result <- repo.getSets
    } yield result == TestHelper.testSets)


  repositories.foreach { r =>
    test(s"Given some sets I can insert them with $r") {
      insertTest(r)
    }
    test(s"Given some sets I can retrieve them with $r") {
      getTest(r)
    }
  }
}