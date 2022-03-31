package io.github.locl95.smashtools.smashgg.repository

import cats.effect.{Blocker, Async, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.JdbcTestTransactor
import io.github.locl95.smashtools.smashgg.{EntrantInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class EntrantsRepositorySpec extends CatsEffectSuite{

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, EntrantInMemoryRepository[F]] =
    Resource.eval(EntrantInMemoryRepository[F])
  private def postgres[F[_]: Async: ContextShift]: Resource[F, EntrantPostgresRepository[F]] =
    for {
      blocker <- Blocker.apply
      transactor <- {
        implicit val b: Blocker = blocker
        JdbcTestTransactor.transactorResource(databaseConf)
      }
    } yield new EntrantPostgresRepository[F](transactor)
  private def repositories[F[_]: Async: ContextShift]: List[(String, Resource[F, EntrantRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

  private val insertTest = (repo: EntrantRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.entrants)
    } yield result == TestHelper.entrants.size)

  private val getTest = (repo: EntrantRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.entrants)
      result <- repo.getEntrants
    } yield result.take(TestHelper.entrants.size) == TestHelper.entrants)

  repositories[IO].foreach { case (name, r) =>
    test(s"Given some Entrants I can insert them with $name"){
      r.use(repo => insertTest(repo))
    }
    test(s"Given some Entrants in repo $name I can retrieve them"){
      r.use(repo => getTest(repo))
    }
  }
}
