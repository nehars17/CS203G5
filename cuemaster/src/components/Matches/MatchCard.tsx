import React from "react";
import {Card, Row, Col, Button} from "react-bootstrap";
import PlayerCard from './PlayerCard';
import { getAuthToken } from '../authUtils';

interface MatchCardData {
    player1: string;  // Player 1 name
    player2: string;  // Player 2 name
    status: string;   // Status of the match (e.g., "UPCOMING", "COMPLETED")
    winner: string | null; // Winner of the match, null if not yet decided
}

const MatchCard: React.FC<MatchCardData> = ({player1, player2, status, winner}) => {
    return (

    //     <div>
    //     <h2>Tournament Matches</h2>
    //     <ul>
    //         {matches.map((match, index) => (
    //             <li key={index}>
    //                 {match.player1} vs {match.player2} - {match.date}
    //             </li>
    //         ))}
    //     </ul>
    // </div>
        <Card className="">
            <Card.Body>
                <Card.Title>{status === 'COMPLETED' ? `Winner: ${winner}` : 'Match Status: ' + status}</Card.Title>

                <Row>
                    <Col xs = {5}>
                        <PlayerCard playerName={player1}/>
                    </Col>
                    <Col xs = {2} className="text-center">
                    </Col>
                    <Col xs = {5}>
                        <PlayerCard playerName={player2}/>
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    );
};

export default MatchCard;