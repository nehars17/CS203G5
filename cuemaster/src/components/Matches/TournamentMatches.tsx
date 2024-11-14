import React, { useEffect, useState } from 'react';
import './TournamentMatches.css';
import { useNavigate, useLocation } from 'react-router-dom';

interface Match {
    id: string;
    player1: string;
    player2: string;
    winner?: string;
}

type Round = 'ROUND_OF_32' | 'ROUND_OF_16' | 'QUARTER_FINALS' | 'SEMIFINALS' | 'FINAL';

const rounds: Round[] = ['ROUND_OF_32', 'ROUND_OF_16', 'QUARTER_FINALS', 'SEMIFINALS', 'FINAL'];

const TournamentMatches: React.FC = () => {
    const [matches, setMatches] = useState<{ [key in Round]?: Match[] }>({});
    const [currentRound, setCurrentRound] = useState<Round>('ROUND_OF_32');
    const navigate = useNavigate();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    // const tournamentId = queryParams.get('id'); 
    
    // Helper function to shuffle an array randomly
    const shuffleArray = <T,>(array: T[]): T[] => {
        const shuffled = [...array];
        for (let i = shuffled.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
        }
        return shuffled;
    };

    // Initialize players for Round of 32 with random pairing
    useEffect(() => {
        const players = Array.from({ length: 32 }, (_, i) => `Player ${i + 1}`);
        const shuffledPlayers = shuffleArray(players);
        const initialMatches = Array.from({ length: 16 }, (_, i) => ({
            id: `match-${i}`,
            player1: shuffledPlayers[i * 2],
            player2: shuffledPlayers[i * 2 + 1],
        }));
        setMatches({ ROUND_OF_32: initialMatches });
    }, []);

    // Generate next round matches based on winners of current round
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

        setMatches(prevMatches => ({
            ...prevMatches,
            [nextRound]: nextRoundMatches,
        }));
        setCurrentRound(nextRound);
    };

    // Declare winner for a match
    const declareWinner = (round: Round, matchId: string, winner: string) => {
        setMatches(prevMatches => ({
            ...prevMatches,
            [round]: prevMatches[round]?.map(match =>
                match.id === matchId ? { ...match, winner } : match
            ),
        }));
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
