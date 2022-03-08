<?php

class Smashgg_graphql_model extends CI_Model {

    private $ch;
    private $graphQLUrl = 'https://api.smash.gg/gql/alpha';

    private function api_call($data){
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $this->graphQLUrl);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type:application/json', 'Authorization: Bearer '.SMASHGG_TOKEN));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
        $result = curl_exec($ch);
        if ($result === FALSE) {
            die('Curl failed: ' . curl_error($ch));
        }
        curl_close($ch);
        return $result;
    }

    public function get_tournament($tournament){
        $data = array("query" => "query getTournament {tournament(slug: \"$tournament\") {id name slug venueName images {type url} events {id name slug videogame { id } entrantSizeMin } startAt endAt}}");
        $result = $this->api_call($data);
        return  json_decode($result)->data->tournament;
    }

    public function get_participants($tournament){
        $data = array("query" => "query getParticipants {tournament(slug: \"$tournament\") { participants(query : {perPage: 300}) { nodes { entrants { participants { id player { id } } event { id } }}} }}");
        $result = $this->api_call($data);
        $result = json_decode($result);

        if ($result->data->tournament != null) {
            return array_reduce($result->data->tournament->participants->nodes,
            function($carry, $node) {
                if($node->entrants == null){
                    return $carry;
                }
                else {
                    return array_merge($carry, array_map(function($entrant) {
                        $entrant->participants = array_map(function($v){return $v->player->id; }, $entrant->participants);
                        return $entrant;
                    }, $node->entrants));
                }
            }, array());
        } else {
            return array();
        }
    }

    public function get_event($tournament,$event){
        $pag = 1;
        $result = null;
        do {
            $data = array("query" => "query getTournament {event(slug: \"tournament/$tournament/event/$event\")  { name state id sets (page:$pag perPage: 50){ nodes { id  phaseGroup { id }  round identifier totalGames fullRoundText  completedAt  slots { entrant { id name} standing { stats { score {value}}}} }} }}");
            $resultPage = json_decode($this->api_call($data));
            // if ($resultPage->data->event->state != "CREATED") {
            //     break;
            // }
            if ($result != null) {
                $result->data->event->sets->nodes = array_merge($result->data->event->sets->nodes, $resultPage->data->event->sets->nodes);
            } else {
                $result = $resultPage;
            }
            $pag+=1;
        } while ($result->data->event != null &&  count($resultPage->data->event->sets->nodes) == 50);

        return $result->data->event;
    }

    public function get_entrants($tournament,$event){
        $data = array("query" => "query getEntrants {event(slug: \"tournament/$tournament/event/$event\") { id entrants(query: {perPage: 500}) { nodes { id name participants { id player { id } } }}}}");
        $result = $this->api_call($data);
        $result = json_decode($result);
        if ($result->data->event != null) {
            $fixEntrants = array_map(function($node) {
                $node->participants = array_map(function($v){return $v->player->id; }, $node->participants);
                return $node;
            }, $result->data->event->entrants->nodes);
            return array("eventId" => $result->data->event->id, "entrants" => $fixEntrants);
        } else {
            return array();
        }
        
    }

    public function get_phases($tournament, $event) {
        $data = array("query" => "query getPhases{event(slug: \"tournament/$tournament/event/$event\") { phases { id name bracketType phaseGroups { nodes { id wave { id } }}}}}");
        $result = $this->api_call($data);
        return  json_decode($result)->data->event->phases;
    }

    public function get_standings($tournament,$event){
        $pag = 1;
        $result = null;
        do {
            $data =  array("query" => "query getTournament {event(slug: \"tournament/$tournament/event/$event\") { id standings(query: {page: $pag perPage: 50} ) { nodes { placement entrant { id participants { gamerTag player { id }}}}}}}");
            $resultPage = json_decode($this->api_call($data));
            if ($resultPage->data->event->standings->nodes == null) {
                break;
            }

            if ($result != null) {
                $result->data->event->standings->nodes = array_merge($result->data->event->standings->nodes, $resultPage->data->event->standings->nodes);
            } else {
                $result = $resultPage;
            }
            $pag+=1;
        } while ($result->data->event != null &&  count($resultPage->data->event->standings->nodes) == 50);
        return $result->data->event->standings->nodes;
    }
}

?>
