import React, { useEffect, useState } from 'react';
import './TournamentMatches.css';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios'; // You can use axios to fetch data from the API

interface Match {
    id: string;
    player1: string;
    player2: string;
    winner?: string;
}

type Round = 'ROUND_OF_32' | 'ROUND_OF_16' | 'QUARTER_FINALS' | 'SEMIFINALS' | 'FINAL';

const rounds: Round[] = ['ROUND_OF_32', 'ROUND_OF_16', 'QUARTER_FINALS', 'SEMIFINALS', 'FINAL'];

const TournamentMatches: React.FC = () => {
    //const { tournamentId } = useParams();
    const [matches, setMatches] = useState<{ [key in Round]?: Match[] }>({});
    const [currentRound, setCurrentRound] = useState<Round>('ROUND_OF_32');
    const navigate = useNavigate();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const tournamentId = queryParams.get('id'); // Fetch tournament ID from query params

    useEffect(() => {
        if (tournamentId) {
            // Fetch the tournament data (including status)
            axios
                .get(`/api/tournaments/${tournamentId}`)
                .then(response => {
                    const fetchedTournament = response.data as { status: string };
                    const tournamentStatus = fetchedTournament.status; // Get the status of the tournament

                    // Now fetch the matches
                    axios
                        .get(`/api/matches/tournament/${tournamentId}`)
                        .then(response => {
                            const fetchedMatches = response.data as Match[];
                            const matchesByStatus = groupMatchesByStatus(fetchedMatches, tournamentStatus);
                            setMatches(matchesByStatus);
                        })
                        .catch(error => {
                            console.error('Error fetching matches:', error);
                        });
                })
                .catch(error => {
                    console.error('Error fetching tournament:', error);
                });
        }
    }, [tournamentId]);


    // Helper function to group matches by tournament status
    const groupMatchesByStatus = (matches: Match[], tournamentStatus: string) => {
        return matches.reduce((acc, match) => {
            // Grouping based on tournament status rather than match.round
            if (!acc[tournamentStatus]) acc[tournamentStatus] = [];
            acc[tournamentStatus].push(match);
            return acc;
        }, {} as { [key: string]: Match[] });
    };

    // Generate next round matches based on winners of the current round
    const generateNextRound = (round: Round) => {
        const roundMatches = matches[round];
        if (!roundMatches || roundMatches.some(match => !match.winner)) {
            alert(`Please declare winners for all matches in ${round} first.`);
            return;
        }

        const nextRoundIndex = rounds.indexOf(round) + 1;
        const nextRound = rounds[nextRoundIndex] as Round;
        const nextRoundMatches = roundMatches.reduce<Match[]>((acc, match, idx) => {
            if (idx % 2 === 0) {
                acc.push({
                    id: `${nextRound}-match-${idx / 2}`,
                    player1: match.winner!,
                    player2: roundMatches[idx + 1].winner!,
                });
            }
            return acc;
        }, []);

        // Save new round matches via the API
        if (tournamentId) {
            axios
                .post(`/api/matches/tournament/${tournamentId}`, nextRoundMatches)
                .then(response => {
                    console.log('Next round matches saved:', response.data);
                })
                .catch(error => {
                    console.error('Error saving next round matches:', error);
                });
        }

        setMatches(prevMatches => ({
            ...prevMatches,
            [nextRound]: nextRoundMatches,
        }));
        setCurrentRound(nextRound);
    };

    // Declare winner for a match and update via API
    const declareWinner = (round: Round, matchId: string, winner: string) => {
        // Update the match locally
        setMatches(prevMatches => ({
            ...prevMatches,
            [round]: prevMatches[round]?.map(match =>
                match.id === matchId ? { ...match, winner } : match
            ),
        }));

        // Save the winner to the backend API
        if (tournamentId) {
            axios
                .post(`/api/matches/${matchId}/winner/${winner}`)
                .then(response => {
                    console.log('Winner declared:', response.data);
                })
                .catch(error => {
                    console.error('Error declaring winner:', error);
                });
        }
    };

    // Navigate back to the tournament page
    const handleSave = () => {
        navigate('/tournaments');
    };

    return (
        <div className="matches-display">
            <h2>Tournament Matches</h2>

            <div className="info-box">
                <p>Please correctly declare the winner of each match.</p>
            </div>

            {rounds.map(round => (
                <div className="round-section" key={round}>
                    <h3>{round.replace('_', ' ')}</h3>
                    <div className="match-grid">
                        {matches[round] ? (
                            matches[round]?.map(match => (
                                <div className="match-card" key={match.id}>
                                    <p>{match.player1} vs {match.player2}</p>
                                    <div className="player-options">
                                        <label>
                                            <input
                                                type="radio"
                                                name={`winner-${match.id}`}
                                                onChange={() => declareWinner(round, match.id, match.player1)}
                                                checked={match.winner === match.player1}
                                            />
                                            {match.player1}
                                        </label>
                                        <label>
                                            <input
                                                type="radio"
                                                name={`winner-${match.id}`}
                                                onChange={() => declareWinner(round, match.id, match.player2)}
                                                checked={match.winner === match.player2}
                                            />
                                            {match.player2}
                                        </label>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p>No matches available for this round.</p>
                        )}
                    </div>
                    {currentRound === round && nextRoundExists(round) && (
                        <button
                            className="generate-button"
                            onClick={() => generateNextRound(round)}
                        >
                            Generate {getNextRoundLabel(round)}
                        </button>
                    )}
                </div>
            ))}
            <button className="save-button" onClick={handleSave}>Save and Go Back to Tournament</button>
        </div>
    );
};

// Helper function to check if there is a next round
const nextRoundExists = (round: Round) => rounds.indexOf(round) < rounds.length - 1;

// Helper function to get label for the next round
const getNextRoundLabel = (round: Round) => {
    const nextIndex = rounds.indexOf(round) + 1;
    return nextIndex < rounds.length ? rounds[nextIndex].replace('_', ' ') : '';
};

export default TournamentMatches;