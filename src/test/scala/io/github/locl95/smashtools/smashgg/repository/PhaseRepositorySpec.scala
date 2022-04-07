package io.github.locl95.smashtools.smashgg.repository

import cats.effect.{Blocker, Async, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.JdbcTestTransactor
import io.github.locl95.smashtools.smashgg.{PhaseInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class PhaseRepositorySpec extends CatsEffectSuite{

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, PhaseInMemoryRepository[F]] =
    Resource.eval(PhaseInMemoryRepository[F])
  private def postgres[F[_]: Async: ContextShift]: Resource[F, PhasePostgresRepository[F]] =
    for {
      blocker <- Blocker.apply
      transactor <- {
        implicit val b: Blocker = blocker
        JdbcTestTransactor.transactorResource(databaseConf)
      }
    } yield new PhasePostgresRepository[F](transactor)
  private def repositories[F[_]: Async: ContextShift]: List[(String, Resource[F, PhaseRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

  private val insertTest = (repo: PhaseRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.phases)
    } yield result == TestHelper.phases.size)

  private val getTest = (repo: PhaseRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.phases)
      result <- repo.getPhases
    } yield result.take(TestHelper.phases.size) == TestHelper.phases)

  repositories[IO].foreach { case (name, r) =>
    test(s"Given some Phases I can insert them with $name"){
      r.use(repo => insertTest(repo))
    }
    test(s"Given some Phases in repo $name I can retrieve them"){
      r.use(repo => getTest(repo))
    }
  }

}
