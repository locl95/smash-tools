package io.github.locl95.smashtools.smashgg.service

import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.Tournament
import io.github.locl95.smashtools.smashgg.repository.TournamentRepository

final case class TournamentService[F[_]](tournamentRepository: TournamentRepository[F],
                                               client: SmashggClient[F]) {

  def insert(tournament: Tournament): F[Int] =
    tournamentRepository.insert(tournament)

  def getTournament(tournamentID: Int): F[Option[Tournament]] =
    tournamentRepository.get(tournamentID)

  def get: F[List[Tournament]] =
    tournamentRepository.get

}
