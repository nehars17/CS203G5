import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Accordion, Button, Card } from 'react-bootstrap'; // Import Bootstrap components


//show matches belonging to a tournament
const TournamentMatches: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    // Tournament State
    const [tournamentName, setTournamentName] = useState('');
    const [matches, setMatches] = useState<{ id: string; player1: string; player2: string; score?: string }[]>([]);


    // Placeholder token for testing
    const token = "mockTokenForTesting"; // Replace with any string for testing

    // Fetch Matches for the Tournament
    useEffect(() => {
        const fetchMatches = async () => {
            try {
                // const token = getAuthToken();
                const response = await fetch(`http://localhost:8080/tournaments/${id}/matches`, {
                    headers: { 'Authorization': `Bearer ${token}` },
                });

                if (response.ok) {
                    const matchData = await response.json();
                    setMatches(matchData);
                } else {
                    console.error('Failed to fetch matches:', response.statusText);
                }
            } catch (error) {
                console.error('Error fetching matches:', error);
            }
        };
        
        fetchMatches();
    }, [id]);

    function editMatch(id: string): void {
        throw new Error('Function not implemented.');
    }

    // Rendering Matches in Accordion
    return (
        <div className="container mt-4">
            {/* Other form fields and update functionality */}

            <Accordion defaultActiveKey="0" className="mt-5">
                <Accordion.Item eventKey="0">
                    <Accordion.Header>Matches for {tournamentName}</Accordion.Header>
                    <Accordion.Body>
                        {matches.length > 0 ? (
                            <div>
                                {matches.map((match, index) => (
                                    <Card key={match.id} className="mb-3">
                                        <Card.Body>
                                            <div>{match.player1} vs {match.player2}</div>
                                            <div>Score: {match.score || 'N/A'}</div>
                                            <Button 
                                                variant="secondary" 
                                                className="mt-2"
                                                onClick={() => editMatch(match.id)}
                                            >
                                                Edit Match
                                            </Button>
                                        </Card.Body>
                                    </Card>
                                ))}
                            </div>
                        ) : (
                            <div>No matches available for this tournament.</div>
                        )}
                    </Accordion.Body>
                </Accordion.Item>
            </Accordion>
        </div>
    );
};

export default TournamentMatches;
