import React, { useEffect, useState } from 'react';
import { getAuthToken } from '../authUtils';

interface Match {
    status: string;
    id: string;
    player1: string;
    player2: string;
    winner?: string;
}

interface TournamentMatchesProps {
    tournamentId: string;
}

const TournamentMatches: React.FC<TournamentMatchesProps> = ({ tournamentId }) => {
    const [matches, setMatches] = useState<Match[]>([]);

    // Fetch Matches for the Tournament
    const fetchMatches = async () => {
        try {
            const token = getAuthToken();
            const response = await fetch(`http://localhost:8080/matches/tournament/${tournamentId}`, {
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

    // Generate Matches for a specific round
    const generateMatchesForRound = async (round: string) => {
        try {
            const token = getAuthToken();
            const response = await fetch(`http://localhost:8080/matches/tournament/${tournamentId}`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ tournamentId, round })
            });

            if (response.ok) {
                fetchMatches(); // Refresh matches after generation
            } else {
                console.error('Failed to generate matches:', response.statusText);
            }
        } catch (error) {
            console.error('Error generating matches:', error);
        }
    };

    useEffect(() => {
        fetchMatches();
    }, [tournamentId]);

    return (
        <div className="matches-display">
            <h2>Matches</h2>
            {['ROUND_OF_32', 'ROUND_OF_16', 'QUARTER_FINALS', 'SEMIFINALS', 'FINAL', 'COMPLETED'].map((status) => (
                <div className="accordion-item" key={status}>
                    <h2 className="accordion-header" id={`heading-${status}`}>
                        <button
                            style={{ fontWeight: 'bold', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', cursor: 'pointer' }}
                            type="button"
                            data-bs-toggle="collapse"
                            data-bs-target={`#collapse-${status}`}
                            aria-expanded="true"
                            aria-controls={`collapse-${status}`}
                        >
                            {status.replace('_', ' ')}
                        </button>
                    </h2>
                    <div
                        id={`collapse-${status}`}
                        className="accordion-collapse collapse show"
                        aria-labelledby={`heading-${status}`}
                        data-bs-parent="#matchesAccordion"
                    >
                        <div className="accordion-body">
                            {/* Generate Button with Inline Styling */}
                            <button 
                                style={{ display: 'block', marginBottom: '10px', padding: '8px 12px', backgroundColor: '#28a745', color: 'white', border: 'none', cursor: 'pointer' }}
                                onClick={() => generateMatchesForRound(status)}
                            >
                                + Generate
                            </button>
                            
                            {/* Display Matches */}
                            {matches.filter((match) => match.status === status).length > 0 ? (
                                <ul>
                                    {matches
                                        .filter((match) => match.status === status)
                                        .map((match, idx) => (
                                            <li key={idx}>
                                                {match.player1} vs {match.player2}
                                            </li>
                                        ))}
                                </ul>
                            ) : (
                                <p>No matches available for this round.</p>
                            )}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default TournamentMatches;
