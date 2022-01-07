package io.github.locl95.smashtools.dailyruleset.protocol

import cats.effect.Sync
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Decoder, Encoder, HCursor}
import io.github.locl95.smashtools.dailyruleset.domain.{TwitterRequest, TwitterResponse}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

object Twitter {

  implicit val twitterResponseDecoder: Decoder[TwitterResponse] = (c: HCursor) => {
    for {
      id <- c.downField("data").downField("id").as[String]
      text <- c.downField("data").downField("text").as[String]
    } yield TwitterResponse(id, text)
  }
  implicit def twitterResponseEntityDecoder[F[_]: Sync]: EntityDecoder[F, TwitterResponse] = jsonOf

  implicit val twitterRequestEncoder: Encoder[TwitterRequest] = deriveEncoder[TwitterRequest]
  implicit def twitterRequestEntityEncoder[F[_]]: EntityEncoder[F, TwitterRequest] = jsonEncoderOf
}
