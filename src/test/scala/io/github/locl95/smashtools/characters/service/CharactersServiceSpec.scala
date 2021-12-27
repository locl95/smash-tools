package io.github.locl95.smashtools.characters.service

import cats.effect.{IO, Sync}
import io.github.locl95.smashtools.characters.domain.{KuroganeCharacter, KuroganeCharacterMove}
import io.github.locl95.smashtools.characters.repository.{CharactersRepository, MovementsRepository}
import munit.CatsEffectSuite
import cats.implicits._
import io.github.locl95.smashtools.characters.{KuroganeClient, TestHelper}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.collection.mutable
import scala.concurrent.ExecutionContext.global

final class CharactersInMemoryRepository[F[_]: Sync] extends CharactersRepository[F] {
  private val charactersList: mutable.ArrayDeque[KuroganeCharacter] = mutable.ArrayDeque.empty

  override def insert(characters: List[KuroganeCharacter]): F[Int] = {
    charactersList.appendAll(characters).pure[F]
    1.pure[F]
  }

  override def isCached: F[Boolean] = false.pure[F]

  override def get: F[List[KuroganeCharacter]] = charactersList.toList.pure[F]

  override def cache: F[Int] = 1.pure[F]
}

final class MovementsInMemoryRepository[F[_]: Sync] extends MovementsRepository[F] {
  private val movementsList: mutable.ArrayDeque[KuroganeCharacterMove] = mutable.ArrayDeque.empty

  override def insert(movements: List[KuroganeCharacterMove]): F[Int] = {
    movementsList.appendAll(movements).pure[F]
    1.pure[F]
  }

  override def isCached: F[Boolean] = false.pure[F]

  override def get: F[List[KuroganeCharacterMove]] = movementsList.toList.pure[F]

  override def cache: F[Int] = 1.pure[F]
}


class CharactersServiceSpec extends CatsEffectSuite {
  test("Get Characters should retrieve them from api and insert them in database when cache is false") {
    val repository = new CharactersInMemoryRepository[IO]
    val program = for {
      client <- BlazeClientBuilder[IO](global).stream
      kuroganeClient = KuroganeClient.impl(client)
      apiCharacters <- fs2.Stream.eval(CharactersService(repository, kuroganeClient).get)
      dbCharacters <- fs2.Stream.eval(repository.get)
    } yield (apiCharacters, dbCharacters)
    assertIO(
      program.compile.foldMonoid.map(_._1.take(2)),
      TestHelper.characters
    )
    assertIO(
      program.compile.foldMonoid.map(_._2.take(2)),
      TestHelper.characters
    )

  }

  test("Get Movements should retrieve them from api and insert them in database when cache is false") {
    val repository = new MovementsInMemoryRepository[IO]
    val program = for {
      client <- BlazeClientBuilder[IO](global).stream
      kuroganeClient = KuroganeClient.impl(client)
      apiMovements <- fs2.Stream.eval(MovementsService(repository, kuroganeClient).get("Joker"))
      dbMovements <- fs2.Stream.eval(repository.get)
    } yield (apiMovements, dbMovements)

    assertIO(
      program.compile.foldMonoid.map(_._1.take(2)),
      TestHelper.movements
    )
    assertIO(
      program.compile.foldMonoid.map(_._2.take(2)),
      TestHelper.movements
    )
  }
}
