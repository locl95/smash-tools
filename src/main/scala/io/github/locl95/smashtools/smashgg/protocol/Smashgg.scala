package io.github.locl95.smashtools.smashgg.protocol

import cats.Traverse
import cats.effect.Sync
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.github.locl95.smashtools.smashgg.domain.{Entrant, Event, Participant, Phase, PlayerStanding, Score, Sets, SmashggQuery, Tournament}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

object Smashgg {
  implicit val smashggQueryEncoder: Encoder[SmashggQuery] = deriveEncoder[SmashggQuery]

  implicit def smashggQueryEntityEncoder[F[_]]: EntityEncoder[F, SmashggQuery] = jsonEncoderOf

  implicit val tournamentDecoder: Decoder[Tournament] = (c: HCursor) => {
    for {
      id <- c.downField("data").downField("tournament").downField("id").as[Int]
      name <- c.downField("data").downField("tournament").downField("name").as[String]
    } yield Tournament(id,name)
  }

  implicit def tournamentEntityDecoder[F[_] : Sync]: EntityDecoder[F, Tournament] = jsonOf

  implicit val participantsDecoder: Decoder[List[Participant]] = (c: HCursor) => {
    for {
      nodesJson <- c
        .downField("data")
        .downField("tournament")
        .downField("participants")
        .downField("nodes")
        .as[List[Json]]
      entrantsJson <-
        Traverse[List]
          .traverse(nodesJson) { item =>
            item
              .hcursor.downField("entrants")
              .as[List[Json]]
          }
      participantsJson <-
        Traverse[List]
          .traverse(entrantsJson) { item =>
            Traverse[List]
              .traverse(item) { i =>
                i
                  .hcursor
                  .downField("participants")
                  .as[List[Json]]
              }
          }
      ids <- Traverse[List]
        .traverse(participantsJson) { item =>
          Traverse[List]
            .traverse(item) { i =>
              Traverse[List]
                .traverse(i) { i2 =>
                  i2
                    .hcursor
                    .downField("id")
                    .as[Int]
                }
            }
        }
    } yield ids.flatten.map(Participant)
  }

  implicit def participantsEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Participant]] = jsonOf

  implicit val eventDecoder: Decoder[Event] = (c: HCursor) => {
    for {
      id <- c.downField("data").downField("event").downField("id").as[Int]
      name <- c.downField("data").downField("event").downField("name").as[String]
    } yield Event(id, name)
  }

  implicit def eventEntityDecoder[F[_] : Sync]: EntityDecoder[F, Event] = jsonOf

  implicit val entrantsDecoder: Decoder[List[Entrant]] = (c: HCursor) => {
    for {
      idEvent <- c
        .downField("data")
        .downField("event")
        .downField("id").as[Int]
      nodes <- c
        .downField("data")
        .downField("event")
        .downField("entrants")
        .downField("nodes")
        .as[List[Json]]
      entrantsName <- Traverse[List].traverse(nodes) { item =>
        item.hcursor.downField("name").as[String]
      }
      entrantsId <- Traverse[List].traverse(nodes) {
        item => item.hcursor.downField("id").as[Int]
      }
    } yield entrantsName.zip(entrantsId).map(x => Entrant(x._2, idEvent, x._1))
  }

  implicit def entrantsEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Entrant]] = jsonOf

  implicit val standingsDecoder: Decoder[List[PlayerStanding]] = (c: HCursor) => {
    for {
      nodes <- c
        .downField("data")
        .downField("event")
        .downField("standings")
        .downField("nodes")
        .as[List[Json]]
      playerPlacements <- Traverse[List].traverse(nodes) {item =>
        item.hcursor.downField("placement").as[Int]
      }
      idEntrant <- Traverse[List].traverse(nodes) {
        item => item.hcursor
          .downField("entrant")
          .downField("id")
          .as[Int]
      }
    } yield playerPlacements.zip(idEntrant).map(x => PlayerStanding(x._1, x._2))
  }

  implicit def standingsEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[PlayerStanding]] = jsonOf

  implicit val phasesDecoder: Decoder[List[Phase]] = (c: HCursor) => {
    for{
      nodes <- c
        .downField("data")
        .downField("event")
        .downField("phases")
        .as[List[Json]]
      id <- Traverse[List].traverse(nodes) {
        item => item.hcursor.downField("id").as[Int]
      }
      name <- Traverse[List].traverse(nodes) {item =>
        item.hcursor.downField("name").as[String]
      }
    } yield id.zip(name).map(x => Phase(x._1, x._2))
  }

  implicit def phaseEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Phase]] = jsonOf

  implicit val setsDecoder: Decoder[List[Sets]] = (c: HCursor) => {
    for {
      idEvent <- c
        .downField("data")
        .downField("event")
        .downField("id")
        .as[Int]

      nodes <- c
        .downField("data")
        .downField("event")
        .downField("sets")
        .downField("nodes")
        .as[List[Json]]

      idSets <- Traverse[List].traverse(nodes) {
        item => item.hcursor.downField("id").as[Int]
      }

      slots <- Traverse[List].traverse(nodes) {
        item => item.hcursor
          .downField("slots")
          .as[List[Json]]
      }

      idPlayers <- Traverse[List].traverse(slots) {
        item => Traverse[List].traverse(item) {
          i => i.hcursor
            .downField("entrant")
            .downField("id")
            .as[Int]
        }
      }

      scorePlayers <- Traverse[List].traverse(slots) {
        item => Traverse[List].traverse(item) {
          i => i.hcursor
            .downField("standing")
            .downField("stats")
            .downField("score")
            .downField("value")
            .as[Int]
        }
      }
    } yield {
      val scores = idPlayers.zip(scorePlayers).map{
        case (id1 :: id2 :: Nil, s1 :: s2 :: Nil) => (Score(id1, s1), Score(id2, s2))
        case _ => (Score(0,0), Score(0,0)) //retornar DecodingFailure
      }
      idSets.zip(scores).map(x => Sets(x._1, idEvent, x._2))
    }
  }
}
