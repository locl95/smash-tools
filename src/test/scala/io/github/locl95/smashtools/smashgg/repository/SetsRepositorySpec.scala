package io.github.locl95.smashtools.smashgg.repository

import cats.effect.{Async, Blocker, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.JdbcTestTransactor
import io.github.locl95.smashtools.smashgg.{SetsInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class SetsRepositorySpec extends CatsEffectSuite {

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, SetsInMemoryRepository[F]] =
    Resource.eval(Sync[F].pure(new SetsInMemoryRepository[F]))
  private def postgres[F[_]: Async: ContextShift]: Resource[F, SetsPostgresRepository[F]] =
    for {
      blocker <- Blocker.apply
      transactor <- {
        implicit val b: Blocker = blocker
        JdbcTestTransactor.transactorResource(databaseConf)
      }
    } yield new SetsPostgresRepository[F](transactor)
  private def repositories[F[_]: Async: ContextShift]: List[(String, Resource[F, SetsRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

  private val insertTest = (repo: SetsRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.testSets)
    } yield result == TestHelper.testSets.size)

  private val getTest = (repo: SetsRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.testSets)
      result <- repo.getSets
    } yield result.take(TestHelper.testSets.size) == TestHelper.testSets)

  repositories[IO].foreach { case (name, r) =>
    test(s"Given some Sets I can insert them with $name"){
      r.use(repo => insertTest(repo))
    }
    test(s"Given some Sets in repo $name I can retrieve them"){
      r.use(repo => getTest(repo))
    }
  }
}