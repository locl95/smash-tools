package io.github.locl95.smashtools.smashgg.repository

import cats.effect.{Async, Blocker, ContextShift, IO, Resource, Sync}
import io.github.locl95.smashtools.JdbcTestTransactor
import io.github.locl95.smashtools.smashgg.{EventInMemoryRepository, TestHelper}
import munit.CatsEffectSuite

class EventsRepositorySpec extends CatsEffectSuite{

  private val databaseConf = TestHelper.databaseTestConfig

  private def inMemory[F[_]: Sync]: Resource[F, EventInMemoryRepository[F]] =
    Resource.eval(Sync[F].pure(new EventInMemoryRepository[F]))
  private def postgres[F[_]: Async: ContextShift]: Resource[F, EventPostgresRepository[F]] =
    for {
      blocker <- Blocker.apply
      transactor <- {
        implicit val b: Blocker = blocker
        JdbcTestTransactor.transactorResource(databaseConf)
      }
    } yield new EventPostgresRepository[F](transactor)
  private def repositories[F[_]: Async: ContextShift]: List[(String, Resource[F, EventRepository[F]])] = List("in memory" -> inMemory, "postgres" -> postgres)

  private val insertTest = (repo: EventRepository[IO]) =>
    assertIOBoolean(for {
      result <- repo.insert(TestHelper.event)
    } yield result == TestHelper.event.id)

  private val getTest = (repo: EventRepository[IO]) =>
    assertIOBoolean(for {
      _ <- repo.insert(TestHelper.event)
      result <- repo.get
    } yield result.take(1) == List(TestHelper.event))

  repositories[IO].foreach { case (name, r) =>
    test(s"Given some Sets I can insert them with $name"){
      r.use(repo => insertTest(repo))
    }
    test(s"Given some Sets in repo $name I can retrieve them"){
      r.use(repo => getTest(repo))
    }
  }
}

