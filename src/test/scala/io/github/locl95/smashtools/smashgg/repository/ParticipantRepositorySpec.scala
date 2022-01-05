package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{ParticipantInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class ParticipantRepositorySpec extends CatsEffectSuite{
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new ParticipantInMemoryRepository[IO]
  private val postgres = new ParticipantPostgresRepository[IO](tx)
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

  private val insertTest = (repo: ParticipantRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.participants)
    } yield result == 3)

  repositories.foreach { r =>
    test(s"Given some participants I can insert them with $r") { insertTest(r) }
    }
}
