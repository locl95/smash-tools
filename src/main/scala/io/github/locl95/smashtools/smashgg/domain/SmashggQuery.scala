package io.github.locl95.smashtools.smashgg.domain

case class SmashggQuery(query: String)

object SmashggQuery {

  def getTournamentQuery(tournament: String): SmashggQuery = SmashggQuery(
    s"""query getTournament {
       |  tournament(slug: "$tournament") {
       |    id
       |    name
       |    slug
       |    venueName
       |    images { type url }
       |    events {
       |      id
       |      name
       |      slug
       |      videogame { id }
       |      entrantSizeMin
       |    }
       |    startAt
       |    endAt
       |  }
       |}""".stripMargin.filter(_ >= ' ')
  )

  def getParticipantsQuery(tournament: String, page: Int): SmashggQuery = SmashggQuery(
    s"""query getParticipants {
       |  tournament(slug: "$tournament") { 
       |    participants(query : {perPage: 64, page: $page}) { 
       |      nodes { 
       |        entrants { 
       |          participants { id player { id } } 
       |          event { id } 
       |        }
       |      }
       |    } 
       |  }
       |}""".stripMargin.filter(_ >= ' ')
  )

  def getEvent(tournament: String, event: String, page: Int): SmashggQuery = SmashggQuery(
    s"""query getTournament {
     | event(slug: \"tournament/$tournament/event/$event\") { 
     |  name 
     |  state 
     |  id 
     |  sets 
     |  (page: $page perPage: 50) { 
     |    nodes { 
     |      id 
     |      phaseGroup { id } 
     |      round 
     |      identifier 
     |      totalGames 
     |      fullRoundText 
     |      completedAt 
     |      slots { 
     |        entrant { id name } 
     |        standing { stats { score {value} } }
     |      } 
     |    }
     |  } 
     |}
     |}""".stripMargin
  )
}
