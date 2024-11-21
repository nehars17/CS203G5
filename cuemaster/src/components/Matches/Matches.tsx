import React, { useEffect, useState } from 'react';
import './Matches.css';
import { useNavigate, useLocation, Navigate } from 'react-router-dom';
import axios from 'axios'; // Axios for API calls
import { isAuthenticated, getUserRole } from '../authUtils';

interface Match {
    id: string;
    player1: string;
    player2: string;
    winner?: string;
    status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
}

const Matches: React.FC = () => {
    const [matches, setMatches] = useState<Match[]>([]);
    const navigate = useNavigate();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const tournamentId = queryParams.get('id'); // Fetch tournament ID 
    const isUserAuthenticated = isAuthenticated();
    const userRole = getUserRole();

    // Ensure useEffect is called before the early return
    useEffect(() => {
        if (tournamentId) {
            // Fetch matches for the tournament from the API
            axios
                .get(`/api/matches/tournament/${tournamentId}`)
                .then(response => {
                    setMatches(response.data as Match[]);
                })
                .catch(error => {
                    console.error('Error fetching matches:', error);
                });
        }
    }, [tournamentId]);

    // If the user is not authenticated or doesn't have the correct role, redirect
    if (!isUserAuthenticated || userRole !== "ROLE_PLAYER") {
        return <Navigate to="/home" />;
    }

    // Group matches by status
    const groupMatchesByStatus = (matches: Match[]) => {
        return {
            notStarted: matches.filter(match => match.status === 'NOT_STARTED'),
            inProgress: matches.filter(match => match.status === 'IN_PROGRESS'),
            completed: matches.filter(match => match.status === 'COMPLETED'),
        };
    };

    const groupedMatches = groupMatchesByStatus(matches);

    // Navigate back to the tournament page
    const handleBackToTournament = () => {
        navigate('/tournaments');
    };

    return (
        <div className="matches-display">
            <h2>Tournament Match Details</h2>

            <div className="info-box">
                <p>Below are all matches for this tournament.</p>
            </div>

            {/* Matches Grouped by Status */}
            {Object.keys(groupedMatches).map((statusKey) => {
                const statusGroup = groupedMatches[statusKey as keyof typeof groupedMatches];
                return (
                    <div className="match-status-group" key={statusKey}>
                        <h3>{statusKey.replace(/([A-Z])/g, ' $1').toUpperCase()}</h3> {/* Capitalize status */}
                        {statusGroup.length > 0 ? (
                            statusGroup.map(match => (
                                <div className="match-card" key={match.id}>
                                    <h4>{match.player1} vs {match.player2}</h4>
                                    <p>Status: {match.status}</p>
                                    <p>{match.winner ? `Winner: ${match.winner}` : 'Winner: TBD'}</p>
                                    <button onClick={() => navigate(`/matches/${match.id}`)}>View Match</button>
                                </div>
                            ))
                        ) : (
                            <p>No matches available in this status.</p>
                        )}
                    </div>
                );
            })}

            <button className="back-button" onClick={handleBackToTournament}>
                Back to Tournament List
            </button>
        </div>
    );
};

export default Matches;
