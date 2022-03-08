package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{PhaseInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class PhaseRepositorySpec extends CatsEffectSuite{
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new PhaseInMemoryRepository[IO]
  private val postgres = new PhasePostgresRepository[IO](tx)
  private val repositories = List(postgres)

  override def beforeEach(context: BeforeEach): Unit = {
    val migrate = for {
      dbProgram <- ctx.databaseProgram
      _ = dbProgram.flyway.clean()
      _ = dbProgram.flyway.migrate()
    } yield ()
    migrate.unsafeRunSync()
    inMemory.clean()
  }

  private val insertTest = (repo: PhaseRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.phases)
    } yield result == 2)

  private val getTest = (repo: PhaseRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.phases)
      result <- repo.getPhases
    } yield result == TestHelper.phases)

  repositories.foreach { r =>
    test(s"Given some phases I can insert them with $r") { insertTest(r) }
    test(s"Given some phases I can retrieve them with $r") { getTest(r) }
  }
}
