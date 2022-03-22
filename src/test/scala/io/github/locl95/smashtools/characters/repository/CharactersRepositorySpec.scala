package io.github.locl95.smashtools.characters.repository

import cats.effect.{Async, Blocker, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.{JdbcTestTransactor}
import io.github.locl95.smashtools.characters.{CharactersInMemoryRepository, TestHelper}
import munit.CatsEffectSuite
import cats.implicits._

class CharactersRepositorySpec extends CatsEffectSuite {

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, CharactersInMemoryRepository[F]] = Resource.eval(Sync[F].pure(new CharactersInMemoryRepository[F]))
  private def postgres[F[_]: Async: ContextShift]: Resource[F, CharacterPostgresRepository[F]] =for {
    blocker <- Blocker.apply
    transactor <- {
      implicit val b: Blocker = blocker
      JdbcTestTransactor.transactorResource(databaseConf)
    }
  } yield new CharacterPostgresRepository[F](transactor)


  private def repositories[F[_]: Async: ContextShift]: List[(String, Resource[F, CharactersRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

  private val insertTest = (repo: CharactersRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.characters)
    } yield result == 2)

  private val getTest = (repo: CharactersRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.characters)
      result <- repo.get
    } yield result == TestHelper.characters)

  private val cacheTest: CharactersRepository[IO] => IO[Unit] = (repo: CharactersRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.cache
      result <- repo.isCached
    } yield result)


  //test(s"Given I have inserted some characters I can retrieve them with $r") { getTest(r) }
  repositories[IO].foreach { case (name, r) =>
    test(s"Given I cached a table, isCached should return true with $name") {
      r.use(repo => cacheTest(repo))
    }
  }

//  repositories.foreach (r => test(s"Given some tournaments I can insert them with $r") { insertTournamentTest(r)})
//  repositories.foreach (r => test(s"Given some tournaments in bdd I can retrieve them with $r") { getTournamentsTest(r)})
//  repositories.foreach (r => test(s"Given some tournaments in bdd I can retrieve certain tournament by with $r") { getTournamentByIdTest(r)})
}
