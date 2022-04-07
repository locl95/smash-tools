package io.github.locl95.smashtools.characters

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{IO, Resource, Sync}
import cats.implicits._
import io.github.locl95.smashtools.JdbcDatabaseConfiguration
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import io.github.locl95.smashtools.characters.repository.{CharactersRepository, MovementsRepository}
import io.github.locl95.smashtools.characters.service.{CharactersService, MovementsService}
import org.http4s.{EntityDecoder, Response, Status}

final class CharactersInMemoryRepository[F[_]: Sync](ref: Ref[F, List[KuroganeCharacter]]) extends CharactersRepository[F] {
  private var cached: Boolean = false

  override def toString: String = "CharactersInMemoryRepository"

  override def insert(characters: List[KuroganeCharacter]): F[Int] =
    for {
      _ <- ref.update(_ ++ characters)
      chars <- ref.get
    } yield chars.size

  override def isCached: F[Boolean] = cached.pure[F]

  override def get: F[List[KuroganeCharacter]] =
    ref.get

  override def cache: F[Int] = {
    cached = true
    1.pure[F]
  }
}

object CharactersInMemoryRepository {
  def apply[F[_]: Sync]: F[CharactersInMemoryRepository[F]] =
    Ref.of[F, List[KuroganeCharacter]](List.empty).map(new CharactersInMemoryRepository[F](_))
}

final class MovementsInMemoryRepository[F[_]: Sync](ref: Ref[F, List[KuroganeCharacterMove]]) extends MovementsRepository[F] {
  override def toString: String = "MovementsInMemoryRepository"
  var cached = false

  override def insert(movements: List[KuroganeCharacterMove]): F[Int] = {
    for {
      _ <- ref.update(_ ++ movements)
      movements <- ref.get
    } yield movements.size
  }

  override def isCached(character: String): F[Boolean] = cached.pure[F]

  override def getMoves(character: String): F[List[KuroganeCharacterMove]] =
    ref.get.map(_.filter(_.character.toLowerCase == character))

  override def cache(character: String): F[Int] = {
    cached = true
    1.pure[F]
  }

  override def getMove(moveId: String): F[Option[KuroganeCharacterMove]] =
    ref.get.map(x => x.find(_.id == moveId))

}

object MovementsInMemoryRepository {
  def apply[F[_]: Sync]: F[MovementsInMemoryRepository[F]] =
    Ref.of[F, List[KuroganeCharacterMove]](List.empty).map(new MovementsInMemoryRepository[F](_))
}

final class KuroganeClientMock[F[_]: Applicative] extends KuroganeClient[F] {

  override def toString: String = "KuroganeClientMock"

  override def getCharacters(implicit decoder: EntityDecoder[F, List[KuroganeCharacter]]): F[List[KuroganeCharacter]] =
    TestHelper.characters.pure[F]

  override def getMovements(character: String)(implicit decoder: EntityDecoder[F, List[KuroganeCharacterMove]]): F[List[KuroganeCharacterMove]] =
    TestHelper.movements.pure[F]
}

object TestHelper {

  def ContextTest: Resource[IO, CharactersRoutes[IO]] =
    {
      val kc: KuroganeClient[IO] = new KuroganeClientMock[IO]

      for {
        repoChar <- Resource.eval(CharactersInMemoryRepository[IO])
        repoMovs <- Resource.eval(MovementsInMemoryRepository[IO])
      } yield new CharactersRoutes[IO](
        CharactersService(repoChar, kc),
        MovementsService(repoMovs, kc)
      )
    }

  val databaseTestConfig: JdbcDatabaseConfiguration = JdbcDatabaseConfiguration("org.postgresql.Driver", "jdbc:postgresql:smashtools", "test", "test", 5, 10)

  val characters: List[KuroganeCharacter] =
    List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))

  val movements: List[KuroganeCharacterMove] =
    List(
      KuroganeCharacterMove("42083468d7124245b6b7f58658bb4843", "Joker", "Jab 1", Some(-16), "ground", Some(4)),
      KuroganeCharacterMove("7a25432add0345549df19a577243a983", "Joker", "Jab 1 (Arsene)", None, "ground", Some(4))
    )

  def checkF[A](actualResp:        Response[IO],
                expectedStatus: Status,
                expectedBody:   Option[A])(
                 implicit ev: EntityDecoder[IO, A]
               ): IO[Boolean] =  {

    val statusCheck        = actualResp.status == expectedStatus
    val bodyCheck          = expectedBody.fold[IO[Boolean]](
      // Verify Response's body is empty.
      actualResp.body.compile.toVector.map(_.isEmpty))(
      expected => actualResp.as[A].map(_ == expected)
    )

    bodyCheck.map(_ && statusCheck)
  }
}


