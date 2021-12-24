package io.github.locl95.smashtools

import org.http4s.Uri
import cats._
import cats.implicits._

object UriHelper {
  def fromString[F[_] : MonadError[*[_], Throwable]](s: String): F[Uri] = Uri.fromString(s) match {
    case Right(value) => value.pure[F]
    case Left(value) => value.getCause.raiseError[F, Uri]
  }
}
