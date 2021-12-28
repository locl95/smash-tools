package io.github.locl95.smashtools

import cats.effect.{Async, ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object SmashtoolsServer {

  def stream[F[_]: ConcurrentEffect: ContextShift](implicit T: Timer[F]): Stream[F, Nothing] = {

    val context = new Context[F]

    for {
      database <- Stream.eval(context.databaseProgram)
      _ = database.flyway.migrate()
      charactersRoutes <- context.charactersRoutesProgram

      httpApp = (charactersRoutes.characterRoutes).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      port <- Stream.eval(Async[F].delay(Option(System.getenv("PORT")).getOrElse("8080").toInt))

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(port, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
