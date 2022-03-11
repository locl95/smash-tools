package io.github.locl95.smashtools.smashgg.service

import cats.effect.Sync
import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.{SmashggQuery, Tournament}
import io.github.locl95.smashtools.smashgg.repository.TournamentRepository
import io.github.locl95.smashtools.smashgg.protocol.Smashgg._
import cats.implicits._

final case class TournamentService[F[_]: Sync](tournamentRepository: TournamentRepository[F],
                                               client: SmashggClient[F]) {

  def insert(tournament: String): F[Int] =
    for {
      tournament <- client.get[Tournament](SmashggQuery.getTournamentQuery(tournament))
      id <- tournamentRepository.insert(tournament)
    } yield id

  def getTournament(tournamentID: Int): F[Option[Tournament]] =
    tournamentRepository.get(tournamentID)

  def get: F[List[Tournament]] =
    tournamentRepository.get

}
