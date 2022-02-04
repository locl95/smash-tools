package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{PlayerStandingInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class PlayerStandingRepositorySpec extends CatsEffectSuite{
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new PlayerStandingInMemoryRepository[IO]
  private val postgres = new PlayerStandingPostgresRepository[IO](tx)
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

  private val insertTest = (repo: PlayerStandingRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.playerStandings)
    } yield result == 2)

  repositories.foreach { r =>
    test(s"Given some player standings I can insert them with $r") { insertTest(r) }
  }
}
