package io.github.locl95.smashtools.smashgg.repository

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.JdbcTestTransactor
import io.github.locl95.smashtools.smashgg.{TestHelper, TournamentsInMemoryRepository}
import munit.CatsEffectSuite

class TournamentRepositorySpec extends CatsEffectSuite{

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, TournamentsInMemoryRepository[F]] =
    Resource.eval(TournamentsInMemoryRepository[F])
  private def postgres[F[_]: ConcurrentEffect: ContextShift]: Resource[F, TournamentPostgresRepository[F]] =
    for {
      blocker <- Blocker.apply
      transactor <- {
        implicit val b: Blocker = blocker
        JdbcTestTransactor.transactorResource(databaseConf)
      }
    } yield new TournamentPostgresRepository[F](transactor)
  private def repositories[F[_]: ConcurrentEffect: ContextShift]: List[(String, Resource[F, TournamentRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

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

  repositories[IO].foreach { case (name, r) =>
    test(s"Given a tournament I can insert it with $name"){
      r.use(repo => insertTournamentTest(repo))
    }
    test(s"Given a tournament in repo $name I can retrieve it"){
      r.use(repo => getTournamentByIdTest(repo))
    }
    test(s"Given tournaments in repo $name i can retrieve them") {
      r.use(repo => getTournamentsTest(repo))
    }
  }
}
