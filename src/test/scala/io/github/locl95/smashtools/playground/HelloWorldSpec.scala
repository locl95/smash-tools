package io.github.locl95.smashtools.playground

import cats.effect.IO
import io.github.locl95.smashtools.playground.SmashtoolsRoutes
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._

class HelloWorldSpec extends CatsEffectSuite {

  test("HelloWorld returns status code 200") {
    assertIO(retHelloWorld.map(_.status) ,Status.Ok)
  }

  test("HelloWorld returns hello world message") {
    assertIO(retHelloWorld.flatMap(_.as[String]), "{\"message\":\"Hello, world\"}")
  }

  private[this] val retHelloWorld: IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/hello/world")
    val helloWorld = HelloWorld.impl[IO]
    SmashtoolsRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW)
  }
}