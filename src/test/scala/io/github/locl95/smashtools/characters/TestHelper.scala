package io.github.locl95.smashtools.characters

import cats.Applicative
import cats.effect.{IO, Sync}
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import io.github.locl95.smashtools.characters.repository.{CharactersRepository, MovementsRepository}
import cats.implicits._
import io.github.locl95.smashtools.JdbcDatabaseConfiguration
import org.http4s.{EntityDecoder, Response, Status}

import scala.collection.mutable

trait InMemoryRepository {
  def clean(): Unit
}

final class CharactersInMemoryRepository[F[_]: Sync] extends CharactersRepository[F] with InMemoryRepository {
  private val charactersList: mutable.ArrayDeque[KuroganeCharacter] = mutable.ArrayDeque.empty
  private var cached: Boolean = false

  override def toString: String = "CharactersInMemoryRepository"

  override def insert(characters: List[KuroganeCharacter]): F[Int] = {
    charactersList.appendAll(characters).pure[F]
    charactersList.size.pure[F]
  }

  override def isCached: F[Boolean] = cached.pure[F]

  override def get: F[List[KuroganeCharacter]] = charactersList.toList.pure[F]

  override def cache: F[Int] = {
    cached = true
    1.pure[F]
  }

  override def clean(): Unit = {
    charactersList.clearAndShrink()
    cached = false
  }
}

final class MovementsInMemoryRepository[F[_]: Sync] extends MovementsRepository[F] with InMemoryRepository {
  private val movementsList: mutable.ArrayDeque[KuroganeCharacterMove] = mutable.ArrayDeque.empty

  override def toString: String = "MovementsInMemoryRepository"

  override def insert(movements: List[KuroganeCharacterMove]): F[Int] = {
    movementsList.appendAll(movements).pure[F]
    movementsList.size.pure[F]
  }

  override def isCached(character: String): F[Boolean] = false.pure[F]

  override def getMoves(character: String): F[List[KuroganeCharacterMove]] =
    movementsList.toList.filter(_.character.toLowerCase == character).pure[F]

  override def cache(character: String): F[Int] = 1.pure[F]

  override def clean(): Unit = {
    movementsList.clearAndShrink()
  }

  override def getMove(moveId: String): F[Option[KuroganeCharacterMove]] = movementsList.find(_.id == moveId).pure[F]
}

final class KuroganeClientMock[F[_]: Applicative] extends KuroganeClient[F] {

  override def toString: String = "KuroganeClientMock"

  override def getCharacters(implicit decoder: EntityDecoder[F, List[KuroganeCharacter]]): F[List[KuroganeCharacter]] =
    TestHelper.characters.pure[F]

  override def getMovements(
      character: String
    )(
      implicit decoder: EntityDecoder[F, List[KuroganeCharacterMove]]
    ): F[List[KuroganeCharacterMove]] = TestHelper.movements.pure[F]
}

object TestHelper {

  val databaseTestConfig = JdbcDatabaseConfiguration("org.postgresql.Driver", "jdbc:postgresql:smashtools", "test", "test", 5, 10)


  val characters: List[KuroganeCharacter] =
    List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))

  val movements: List[KuroganeCharacterMove] =
    List(
      KuroganeCharacterMove("42083468d7124245b6b7f58658bb4843", "Joker", "Jab 1", Some(-16), "ground", Some(4)),
      KuroganeCharacterMove("7a25432add0345549df19a577243a983", "Joker", "Jab 1 (Arsene)", None, "ground", Some(4))
    )

  def check[A](
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A]
    )(
      implicit ev: EntityDecoder[IO, A]
    ): Boolean = {
    val actualResp = actual.unsafeRunSync()
    actualResp.status == expectedStatus && expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync().isEmpty
    )(expected => {
      val a = actualResp.as[A].unsafeRunSync()
      a == expected
    })
  }
}


