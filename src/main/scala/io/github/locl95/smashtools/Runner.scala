package io.github.locl95.smashtools

import cats.effect.{ExitCode, IO, IOApp}

object Runner extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    SmashtoolsServer.stream[IO].compile.drain.as(ExitCode.Success)
  }

}
