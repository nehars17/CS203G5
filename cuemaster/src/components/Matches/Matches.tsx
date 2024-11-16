import React, { useEffect, useState } from 'react';
import { Accordion } from 'react-bootstrap';

interface Match {
    id: number;
    player1: string;
    player2: string;
    winner?: string; 
}

const Matches: React.FC = () => {
    const [allMatches, setAllMatches] = useState<Match[]>([]);

    useEffect(() => {
        // Fetch all matches
        const fetchMatches = async () => {
            try {
                const response = await fetch('http://localhost:8080/matches');
                if (!response.ok) {
                    throw new Error('Failed to fetch matches');
                }
                const data = await response.json();
                setAllMatches(data);
            } catch (error) {
                console.error('Error fetching matches:', error);
            }
        };

        fetchMatches();
    }, []);

    return (
        <div className="container mt-4">
            <h2>All Matches</h2>
            <Accordion>
                {allMatches.map((match) => (
                    <Accordion.Item eventKey={match.id.toString()} key={match.id}>
                        <Accordion.Header>{match.player1} vs {match.player2}</Accordion.Header>
                        <Accordion.Body>
                            <div>Winner: {match.winner || 'N/A'}</div>
                        </Accordion.Body>
                    </Accordion.Item>
                ))}
            </Accordion>
        </div>
    );
};

export default Matches;
