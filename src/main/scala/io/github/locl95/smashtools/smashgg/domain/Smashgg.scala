package io.github.locl95.smashtools.smashgg.domain

case class Tournament(id:Int, name: String)
case class Participant(ids: List[Int]) //Players
case class Player(id:Int)
case class PlayerEntrant(idPlayer:Int, idEntrant:Int)
case class Event(id:Int, name: String)
case class Entrant(id:Int, idEvent:Int, name: String)
case class PlayerStanding(placement: Int, idEntrant: Int)
case class Phase(id: Int, name:String)
case class Sets(id:Int, idEvent:Int, scores: (Score, Score))
case class Score(idPlayer: Int, score: Int)
