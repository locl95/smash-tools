package io.github.locl95.smashtools.smashgg.service

import io.github.locl95.smashtools.smashgg.SmashggClient
import io.github.locl95.smashtools.smashgg.domain.Entrant
import io.github.locl95.smashtools.smashgg.repository.EntrantRepository

final case class EntrantService[F[_]](entrantRepository: EntrantRepository[F],
                                      client: SmashggClient[F]){

  def insert(entrants: List[Entrant]): F[Int] =
    entrantRepository.insert(entrants)

  def getEntrants: F[List[Entrant]] =
    entrantRepository.getEntrants

  def getEntrants(eventID: Int): F[List[Entrant]] =
    entrantRepository.getEntrants(eventID)
}

