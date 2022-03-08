package io.github.locl95.smashtools.smashgg.repository

import cats.effect.IO
import io.github.locl95.smashtools.Context
import io.github.locl95.smashtools.smashgg.{TestHelper, TournamentsInMemoryRepository}
import munit.CatsEffectSuite

class TournamentRepositorySpec extends CatsEffectSuite{
  val ctx = new Context[IO]
  val tx = ctx.databaseProgram.unsafeRunSync().transactor

  private def inMemory = new TournamentsInMemoryRepository[IO]
  private def postgres = new TournamentPostgresRepository[IO](tx)
  private def repositories: List[TournamentRepository[IO]] = List(inMemory, postgres)

  override def beforeEach(context: BeforeEach): Unit = {
    val migrate = for {
      dbProgram <- ctx.databaseProgram
      _ = dbProgram.flyway.clean()
      _ = dbProgram.flyway.migrate()
    } yield ()
    migrate.unsafeRunSync()
  }

  private val insertTournamentTest = (repo: TournamentRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.tournament)
    } yield result == TestHelper.tournament.id)

  private val getTournamentsTest = (repo: TournamentRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.tournament)
      result <- repo.get
    } yield result.take(1) == List(TestHelper.tournament))

  private val getTournamentByIdTest = (repo: TournamentRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.tournament)
      result <- repo.get(312932)
    } yield result.get == TestHelper.tournament)

  repositories.foreach (r => test(s"Given some tournaments I can insert them with $r") { insertTournamentTest(r)})
  repositories.foreach (r => test(s"Given some tournaments in bdd I can retrieve them with $r") { getTournamentsTest(r)})
  repositories.foreach (r => test(s"Given some tournaments in bdd I can retrieve certain tournament by with $r") { getTournamentByIdTest(r)})
}
