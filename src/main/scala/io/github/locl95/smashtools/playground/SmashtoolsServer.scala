package io.github.locl95.smashtools.playground

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.implicits._

import scala.concurrent.ExecutionContext.global
import cats.implicits._
import io.github.locl95.smashtools.characters.{CharactersRoutes, KuroganeClient}

object SmashtoolsServer {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]
      jokeAlg = Jokes.impl[F](client)
      kuroganeClient = KuroganeClient.impl[F](client)

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      httpApp = (
        SmashtoolsRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        SmashtoolsRoutes.jokeRoutes[F](jokeAlg) <+>
          CharactersRoutes.characterRoutes(kuroganeClient)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
