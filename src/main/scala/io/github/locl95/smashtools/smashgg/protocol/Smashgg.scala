package io.github.locl95.smashtools.smashgg.protocol

import cats.Traverse
import cats.effect.Sync
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.github.locl95.smashtools.smashgg.domain.{Event, Participant, SmashggQuery, Tournament}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

object Smashgg {
  implicit val smashggQueryEncoder: Encoder[SmashggQuery] = deriveEncoder[SmashggQuery]

  implicit def smashggQueryEntityEncoder[F[_]]: EntityEncoder[F, SmashggQuery] = jsonEncoderOf

  implicit val tournamentDecoder: Decoder[Tournament] = (c: HCursor) => {
    for {
      name <- c.downField("data").downField("tournament").downField("name").as[String]
    } yield Tournament(name)
  }
  implicit def tournamentEntityDecoder[F[_]: Sync]: EntityDecoder[F, Tournament] = jsonOf

  implicit val participantsDecoder: Decoder[List[Participant]] = (c: HCursor) => {
    for {
      nodesJson <- c
        .downField("data")
        .downField("tournament")
        .downField("participants")
        .downField("nodes")
        .as[List[Json]]
      entrantsJson <- Traverse[List].traverse(nodesJson) { item => item.hcursor.downField("entrants").as[List[Json]] }
      participantsJson <- Traverse[List].traverse(entrantsJson) { item =>
        Traverse[List].traverse(item) { i => i.hcursor.downField("participants").as[List[Json]] }
      }
      ids <- Traverse[List].traverse(participantsJson) { item =>
        Traverse[List].traverse(item) { i => Traverse[List].traverse(i) { i2 => i2.hcursor.downField("id").as[Int] } }
      }
    } yield ids.flatten.map(Participant)
  }
  implicit def participantsEntityDecoder[F[_]: Sync]: EntityDecoder[F, List[Participant]] = jsonOf

  implicit val eventDecoder: Decoder[Event] = (c: HCursor) => {
    for {
      name <- c.downField("data").downField("event").downField("name").as[String]
    } yield Event(name)
  }
  implicit def eventEntityDecoder[F[_]: Sync]: EntityDecoder[F, Event] = jsonOf

}
