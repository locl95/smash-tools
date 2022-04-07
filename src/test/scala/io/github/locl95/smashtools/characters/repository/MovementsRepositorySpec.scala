package io.github.locl95.smashtools.characters.repository

import cats.effect.{Async, Blocker, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.JdbcTestTransactor
import io.github.locl95.smashtools.characters.{MovementsInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class MovementsRepositorySpec extends CatsEffectSuite {

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, MovementsInMemoryRepository[F]] =
    Resource.eval(MovementsInMemoryRepository[F])

  private def postgres[F[_]: Async: ContextShift]: Resource[F, MovementPostgresRepository[F]] =
    for {
      blocker <- Blocker.apply
      transactor <- {
        implicit val b: Blocker = blocker
        JdbcTestTransactor.transactorResource(databaseConf)
      }
    } yield new MovementPostgresRepository[F](transactor)

  private def repositories[F[_]: Async: ContextShift]: List[(String, Resource[F, MovementsRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

  private val insertTest = (repo: MovementsRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.movements)
    } yield result == TestHelper.movements.size)

  private val getTest = (repo: MovementsRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.movements)
      result <- repo.getMoves("joker")
    } yield result.take(TestHelper.movements.size) == TestHelper.movements)

  private val cacheTest: MovementsRepository[IO] => IO[Unit] = (repo: MovementsRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.cache("joker")
      result <- repo.isCached("joker")
    } yield result)

  repositories[IO].foreach { case (name, r) =>
    test(s"Given Joker movements I can insert them $name"){
      r.use(repo => insertTest(repo))
    }
    test(s"Given Joker movements in repo $name I can retrieve them"){
      r.use(repo => getTest(repo))
    }
    test(s"Given I cached a table, isCached should return true with $name") {
      r.use(repo => cacheTest(repo))
    }
  }
}

