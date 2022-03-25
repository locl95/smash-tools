package io.github.locl95.smashtools.smashgg

import cats.Monad
import cats.implicits.catsSyntaxApplicativeId

import scala.collection.mutable

trait CredentialsRepository[F[_]] {
  def insert(credential: Credential): F[Int]
  def get(token: String): F[Either[String, Int]]
}

final class CredentialsInMemoryRepository[F[_]: Monad] extends CredentialsRepository[F] {
  private val credentials: mutable.ArrayDeque[Credential] = mutable.ArrayDeque.empty

  override def get(token: String): F[Either[String, Int]] =
    credentials.find(_.credential == token).map(_.idUser).toRight("Token invalid").pure[F]

  override def insert(credential: Credential): F[Int] = {
    credentials.append(credential)
    credential.idUser.pure[F]
  }
}
