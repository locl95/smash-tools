package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{EntrantInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class EntrantsRepositorySpec extends CatsEffectSuite{

  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new EntrantInMemoryRepository[IO]
  private val postgres = new EntrantPostgresRepository[IO](tx)
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

  private val insertTest = (repo: EntrantRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.entrants)
    } yield result == 2)

  private val getTest = (repo: EntrantRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.entrants)
      result <- repo.getEntrants
    } yield result == TestHelper.entrants)

  repositories.foreach { r =>
    test(s"Given some entrants I can insert them with $r") { insertTest(r)}
    test(s"Given some entrants I can retrieve them with $r") { getTest(r)}
  }
}
