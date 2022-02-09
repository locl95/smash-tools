package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{EventInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class EventsRepositorySpec extends CatsEffectSuite{

  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new EventInMemoryRepository[IO]
  private val postgres = new EventPostgresRepository[IO](tx)
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

  private val insertTest = (repo: EventRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.event)
    } yield result == 1)

  private val getTest = (repo: EventRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.event)
      result <- repo.getEvents
    } yield result == List(TestHelper.event))


  repositories.foreach { r =>
    test(s"Given some events I can insert them with $r") { insertTest(r) }
    test(s"Given some events I can retrieve them with $r") { getTest(r) }
  }
}

