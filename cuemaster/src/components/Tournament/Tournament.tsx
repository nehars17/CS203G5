import React, { useEffect, useState } from 'react';
import API from '../../services/api';
import { Link } from 'react-router-dom';
import { isAuthenticated, getUserRole, getUserIdFromToken } from '../authUtils';
import './Tournament.css';
import config from '../../config';

interface Tournament {
    id: number;
    tournamentname: string;
    startDate: string;
    endDate: string;
    time: string;
    location: string;
    status: string;
    description: string;
    winnerId: number | null; // Assuming winnerId can be null
    players: number[];       // Array of player IDs
}

const Tournaments: React.FC = () => {
    const [tournaments, setTournaments] = useState<Tournament[]>([]);
    const [status, setStatus] = useState('UPCOMING');
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [filter, setFilter] = useState<string>('ALL'); // New filter state
    const [seasonFilter, setSeasonFilter] = useState<string>('ALL'); // New state for season filter
    const isUserAuthenticated = isAuthenticated();
    const userRole = getUserRole();
    const playerId = getUserIdFromToken(); // Get the player ID from the token

    const fetchTournaments = async () => {
        try {
            const response = await API.get<Tournament[]>('/tournaments');
            setTournaments(response.data);
        } catch (error) {
            console.error('Error fetching tournaments', error);
            setError('Failed to fetch tournaments. Please try again later.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTournaments(); // Call fetchTournaments when the component mounts
    }, []);

    const determineSeason = (startDate: string): string => {
        const month = new Date(startDate).getMonth() + 1; // Months are 0-indexed
        if (month >= 10 && month <= 12) return 'Season 1'; // October - December
        if (month >= 1 && month <= 3) return 'Season 2'; // January - March
        if (month >= 4 && month <= 6) return 'Season 3'; // April - June
        return 'Season 4'; // July - September
    };

    // Filter function to filter tournaments based on selected status
    const filteredTournaments = tournaments.filter((tournament) => {
        const season = determineSeason(tournament.startDate);
        const statusMatch = filter === 'ALL' || tournament.status === filter;
        const seasonMatch = seasonFilter === 'ALL' || season === seasonFilter;
        return statusMatch && seasonMatch;
    });

    const handleJoin = async (id: number) => {
        if (!playerId) {
            console.error('Player ID is not available. Unable to join tournament.');
            return;
        }

        const confirmed = window.confirm(`Do you want to join?`);
        if (!confirmed) return;

        try {
            const token = localStorage.getItem('token');
            const response1 = await fetch(`${config.apiBaseUrl}/tournaments/${id}/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(playerId),  // Send playerId as a plain number
            });

            if (response1.ok) {
                console.log(`Player ${playerId} joined tournament ${id}.`);
                alert(`You have successfully joined!`);

                // Fetch updated tournament data (as previously described)
                const updatedResponse = await fetch(`${config.apiBaseUrl}/tournaments/${id}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                const updatedTournament = await updatedResponse.json();
                setTournaments(prevTournaments =>
                    prevTournaments.map(tournament =>
                        tournament.id === id ? updatedTournament : tournament
                    )
                );
            } else {
                console.error('Failed to join tournament:', response1.statusText);
            }
            fetchTournaments();
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const handleLeave = async (id: number) => {
        if (!playerId) {
            console.error('Player ID is not available. Unable to leave tournament.');
            return;
        }

        const confirmed = window.confirm(`Do you want to leave?`);
        if (!confirmed) return;

        try {
            const token = localStorage.getItem('token');
            const response2 = await fetch(`${config.apiBaseUrl}/tournaments/${id}/leave`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(playerId),  // Send playerId as a plain number
            });

            if (response2.ok) {
                console.log(`Player ${playerId} left tournament ${id}.`);
                alert(`You have successfully left!`);

                // Fetch updated tournament data to update state
                const updatedResponse = await fetch(`${config.apiBaseUrl}/tournaments/${id}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                const updatedTournament = await updatedResponse.json();
                setTournaments(prevTournaments =>
                    prevTournaments.map(tournament =>
                        tournament.id === id ? updatedTournament : tournament
                    )
                );
            } else {
                console.error('Failed to leave tournament:', response2.statusText);
            }
            fetchTournaments();
        } catch (error) {
            console.error('Error:', error);
        }
    };


    const handleDelete = async (id: number) => {
        if (window.confirm('Are you sure you want to delete this tournament?')) {
            try {
                const token = localStorage.getItem('token');
                const response = await fetch(`${config.apiBaseUrl}/tournaments/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.ok) {
                    console.log(`Deleted tournament with ID: ${id}`);
                    fetchTournaments();
                } else {
                    console.error('Failed to delete tournament:', response.statusText);
                    setError('Failed to delete tournament. Please try again.');
                }
            } catch (error) {
                console.error('Error:', error);
                setError('An error occurred while deleting the tournament.');
            }
        }
    };


    if (loading) {
        return <div>Loading tournaments...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div className="tournament-container">
            <h1 className="tournament-title">Tournaments</h1>

            {/* Filter Tabs */}
            <div className="tabs">
                {['ALL', 'UPCOMING', 'ONGOING', 'COMPLETED'].map((status) => (
                    <button
                        key={status}
                        onClick={() => setFilter(status)}
                        className={`tab ${filter === status ? 'active-tab' : ''}`}
                    >
                        {status}
                    </button>
                ))}
            </div>

            {/* Season Dropdown */}
            <select value={seasonFilter} onChange={(e) => setSeasonFilter(e.target.value)} className="dropdown">
                <option value="ALL">All Seasons</option>
                <option value="Season 1">Season 1</option>
                <option value="Season 2">Season 2</option>
                <option value="Season 3">Season 3</option>
                <option value="Season 4">Season 4</option>
            </select>

            {/* Show Create Tournament button only for authenticated organizers */}
            {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                <Link to="/tournaments/create-tournament" className="create-button">
                    Create Tournament
                </Link>
            )}

            <ul className="tournament-list">
                {filteredTournaments.map((tournament) => (
                    <li
                        key={tournament.id}
                        className="tournament-item"
                        onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 8px 16px rgba(0, 0, 0, 0.15)'}
                        onMouseLeave={(e) => e.currentTarget.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)'}
                    >
                        <h3 className="tournament-heading">{tournament.tournamentname}</h3>
                        <p className="tournament-details"><strong>Date:</strong> {tournament.startDate} to {tournament.endDate}</p>
                        <p className="tournament-details"><strong>Time:</strong> {tournament.time}</p>
                        <p className="tournament-details"><strong>Location:</strong> {tournament.location}</p>
                        <p className="tournament-details"><strong>Status:</strong> {tournament.status}</p>
                        <p className="tournament-details"><strong>Description:</strong> {tournament.description}</p>
                        <p className="tournament-details"><strong>WinnerID:</strong> {tournament.winnerId}</p>

                        {/* Render Join button */}
                        {isUserAuthenticated && tournament.players.length < 32 && userRole === "ROLE_PLAYER" && playerId !== null && !tournament.players.includes(playerId) && tournament.status === "UPCOMING" && (
                            <button onClick={() => handleJoin(tournament.id)} className="join-button">
                                Join
                            </button>
                        )}

                        {/* Render Leave button */}
                        {isUserAuthenticated && userRole === "ROLE_PLAYER" && playerId !== null && tournament.players.includes(playerId) && tournament.status === "UPCOMING" && (
                            <button onClick={() => handleLeave(tournament.id)} className="leave-button">
                                Leave
                            </button>
                        )}

                        {/* Render buttons only for authenticated organizers */}
                        {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                            <div className="button-container">
                                <Link to={`/tournaments/update-tournament/${tournament.id}`} className="update-button">
                                    Edit
                                </Link>
                                <button
                                    onClick={() => handleDelete(tournament.id)}
                                    className="delete-button"
                                >
                                    Delete
                                </button>
                            </div>
                        )}

                        {/* View Matches Button */}
                        <div className="button-container">
                            {isUserAuthenticated && userRole === "ROLE_PLAYER" && (
                                <Link to={`/tournaments/${tournament.id}/matches`} className="viewMatchesButton">
                                    View Matches
                                </Link>
                            )}
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
};



export default Tournaments;