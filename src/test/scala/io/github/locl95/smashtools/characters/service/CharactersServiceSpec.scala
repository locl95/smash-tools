package io.github.locl95.smashtools.characters.service

import cats.effect.{IO, Sync}
import io.github.locl95.smashtools.characters.domain.KuroganeCharacter
import io.github.locl95.smashtools.characters.repository.CharactersRepository
import munit.CatsEffectSuite
import cats.implicits._
import io.github.locl95.smashtools.characters.KuroganeClient
import org.http4s.client.blaze.BlazeClientBuilder

import scala.collection.mutable
import scala.concurrent.ExecutionContext.global

final class CharactersInMemoryRepository[F[_]: Sync] extends CharactersRepository[F] {
  private val charactersList: mutable.ArrayDeque[KuroganeCharacter] = mutable.ArrayDeque.empty

  override def insert(characters: List[KuroganeCharacter]): F[Int] = {
    charactersList.appendAll(characters).pure[F]
    1.pure[F]
  }

  override def isCached(table: String): F[Boolean] = false.pure[F]

  override def get: F[List[KuroganeCharacter]] = charactersList.toList.pure[F]

  override def cache(table: String): F[Int] = 1.pure[F]
}

class CharactersServiceSpec extends CatsEffectSuite {
  test("Get Characters should insert them in database when cache is false") {
    val repository = new CharactersInMemoryRepository[IO]
    val program = for {
      client <- BlazeClientBuilder[IO](global).stream
      kuroganeClient = KuroganeClient.impl(client)
      apiCharacters <- fs2.Stream.eval(CharactersService(repository, kuroganeClient).getCharacters)
      dbCharacters <- fs2.Stream.eval(repository.get)
    } yield (apiCharacters, dbCharacters)
    assertIO(
      program.compile.foldMonoid.map(_._1.take(2)),
      List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))
    )
    assertIO(
      program.compile.foldMonoid.map(_._2.take(2)),
      List(KuroganeCharacter("Bowser"), KuroganeCharacter("DarkPit"))
    )

  }
}
