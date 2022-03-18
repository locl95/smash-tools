package io.github.locl95.smashtools.characters.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.characters.{MovementsInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class MovementsRepositorySpec extends CatsEffectSuite{
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private val inMemory = new MovementsInMemoryRepository[IO]
  private val postgres = new MovementPostgresRepository[IO](tx)
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

  private val insertTest = (repo: MovementsRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.movements)
    } yield result == TestHelper.movements.length)

  private val getTest = (repo: MovementsRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.movements)
      result <- repo.getMoves("joker")
    } yield result == TestHelper.movements)

  private val cacheTest = (repo: MovementsRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.cache("joker")
      result <- repo.isCached("joker")
    } yield result)

  repositories.foreach (r => test(s"Given some movements I can insert them with $r") { insertTest(r)})
  repositories.foreach (r => test(s"Given some movements I can retrieve them with $r") { getTest(r)})
  repositories.foreach (r => test(s"Given some movements I can cache them with $r") { cacheTest(r)})

}
