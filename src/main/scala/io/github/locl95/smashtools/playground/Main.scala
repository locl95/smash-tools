package io.github.locl95.smashtools.playground

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    SmashtoolsServer.stream[IO].compile.drain.as(ExitCode.Success)
}
