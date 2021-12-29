package io.github.locl95.smashtools.smashgg

import io.github.locl95.smashtools.smashgg.domain.SmashggQuery
import org.http4s.Method.POST
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

trait SmashggClient[F[_]] {
  def get[A](body: SmashggQuery)(implicit encoder: EntityEncoder[F, SmashggQuery], decoder: EntityDecoder[F, A]): F[A]
}

case class SmashggClientError(t: Throwable) extends RuntimeException

object SmashggClient {

  def impl[F[_]](C: Client[F]): SmashggClient[F] = new SmashggClient[F] {

    def get[A](
        body: SmashggQuery
      )(
        implicit encoder: EntityEncoder[F, SmashggQuery],
        decoder: EntityDecoder[F, A]
      ): F[A] = {
      val headers = Headers.apply(
        Header("Authorization", "Bearer 3305177ceda157c60fbc09b79e2ff987")
      )
      val request =
        Request[F](method = POST, uri = uri"https://api.smash.gg/gql/alpha", headers = headers).withEntity(body)

      C.expect[A](request)
    }
  }
}
