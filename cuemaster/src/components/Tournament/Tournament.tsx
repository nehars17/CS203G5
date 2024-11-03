import React, { useEffect, useState } from 'react';
import API from '../../services/api';
import { Link } from 'react-router-dom';
import { isAuthenticated, getUserRole, getUserIdFromToken } from '../authUtils';

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
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [filter, setFilter] = useState<string>('ALL'); // New filter state
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

    // Filter function to filter tournaments based on selected status
    const filteredTournaments = tournaments.filter((tournament) => {
        if (filter === 'ALL') return true;
        return tournament.status === filter;
    });

    const handleJoin = async (id: number) => {
        if (!playerId) {
            console.error('Player ID is not available. Unable to join tournament.');
            return;
        }

        try {
            await joinTournament(id, playerId);
            setTournaments(prevTournaments =>
                prevTournaments.map(tournament =>
                    tournament.id === id ? { ...tournament, players: [...tournament.players, playerId] } : tournament
                )
            );
            console.log(`Player ${playerId} joined tournament ${id}.`);
        } catch (error) {
            console.error('Error joining tournament:', error);
            setError('Failed to join tournament. Please try again later.');
        }
    };

    const handleLeave = async (id: number) => {
        if (!playerId) {
            console.error('Player ID is not available. Unable to leave tournament.');
            return;
        }

        try {
            await leaveTournament(id, playerId);
            setTournaments(prevTournaments =>
                prevTournaments.map(tournament =>
                    tournament.id === id ? { ...tournament, players: tournament.players.filter(player => player !== playerId) } : tournament
                )
            );
            console.log(`Player ${playerId} left tournament ${id}.`);
        } catch (error) {
            console.error('Error leaving tournament:', error);
            setError('Failed to leave tournament. Please try again later.');
        }
    };

    const joinTournament = async (tournamentId: number, playerId: number) => {
        try {
            const authToken = localStorage.getItem('authToken'); // Or wherever you store your token
            const response = await API.post(`/tournaments/${tournamentId}/join`, { playerId }, {
                headers: {
                    Authorization: `Bearer ${authToken}`,
                },
                withCredentials: true,
            });
            console.log('Join response:', response); // Log the response
            return response; // Return the response to be used in the component
        } catch (error) {
            console.error('Error joining tournament:', error);
            throw error; // Throw error to be caught in handleJoin
        }
    };
    
    const leaveTournament = async (tournamentId: number, playerId: number) => {
        try {
            const authToken = localStorage.getItem('authToken'); // Or wherever you store your token
            const response = await API.post(`/tournaments/${tournamentId}/leave`, { playerId }, {
                headers: {
                    Authorization: `Bearer ${authToken}`,
                },
                withCredentials: true,
            });
            console.log('Leave response:', response); // Log the response
            return response; // Return the response to be used in the component
        } catch (error) {
            console.error('Error leaving tournament:', error);
            throw error; // Throw error to be caught in handleLeave
        }
    };
    

    if (loading) {
        return <div>Loading tournaments...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Tournaments</h1>

            {/* Filter Tabs */}
            <div style={styles.tabs}>
                {['ALL', 'UPCOMING', 'ONGOING', 'COMPLETED'].map((status) => (
                    <button
                        key={status}
                        onClick={() => setFilter(status)}
                        style={{
                            ...styles.tab,
                            ...(filter === status ? styles.activeTab : {})
                        }}
                    >
                        {status}
                    </button>
                ))}
            </div>


            {/* Show Create Tournament button only for authenticated organizers */}
            {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                <Link to="/tournaments/create-tournament" className="create-button" style={styles.createButton}>
                    Create Tournament
                </Link>
            )}

            <ul style={styles.tournamentList}>
                {filteredTournaments.map((tournament) => (  // Use filteredTournaments here
                    <li key={tournament.id} style={styles.tournamentItem}>
                        <h3>{tournament.tournamentname}</h3>
                        <p><strong>Date:</strong> {tournament.startDate} to {tournament.endDate}</p>
                        <p><strong>Time:</strong> {tournament.time}</p>
                        <p><strong>Location:</strong> {tournament.location}</p>
                        <p><strong>Status:</strong> {tournament.status}</p>
                        <p><strong>Description:</strong> {tournament.description}</p>
                        <p><strong>WinnerID:</strong> {tournament.winnerId}</p>
                        <p><strong>Players:</strong> {tournament.players.length > 0 ? tournament.players.join(', ') : 'No players'}</p>


                        {/* Render Join button only for authenticated players not already in the tournament */}
                        {isUserAuthenticated && userRole === "ROLE_PLAYER" && playerId !== null && !tournament.players.includes(playerId) && (
                            <button onClick={() => handleJoin(tournament.id)} style={styles.joinButton}>
                                Join
                            </button>
                        )}

                        {/* Render Leave button only for authenticated players in the tournament */}
                        {isUserAuthenticated && userRole === "ROLE_PLAYER" && playerId !== null && tournament.players.includes(playerId) && (
                            <button onClick={() => handleLeave(tournament.id)} style={styles.leaveButton}>
                                Leave
                            </button>
                        )}



                        {/* Render buttons only for authenticated organizers */}
                        {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                            <div style={styles.buttonContainer}>
                                <Link to={`/tournaments/update-tournament/${tournament.id}`} className="update-button" style={styles.updateButton}>
                                    Update
                                </Link>
                                <Link to={`/tournaments/delete-tournament/${tournament.id}`} className="delete-button" style={styles.deleteButton}>
                                    Delete
                                </Link>
                            </div>
                        )}
                    </li>
                ))}
            </ul>
        </div>
    );
};

const styles = {
    container: {
        margin: '20px',
        textAlign: 'center' as const,
    },
    title: {
        fontSize: '2rem',
        marginBottom: '20px',
    },
    tabs: {
        display: 'flex',
        gap: '10px',
    },
    tab: {
        padding: '8px 16px',
        border: '1px solid #ddd',
        borderRadius: '5px',
        backgroundColor: '#f0f0f0',
        cursor: 'pointer',
    },
    activeTab: {
        backgroundColor: '#007bff',
        color: '#fff',
        border: '1px solid #007bff', 
    },
    createButton: {
        display: 'inline-block',
        marginBottom: '20px',
        padding: '10px 20px',
        backgroundColor: '#007bff',
        color: '#fff',
        textDecoration: 'none',
        borderRadius: '5px',
    },
    filterDropdown: {
        marginBottom: '20px',
        padding: '8px 12px',
    },
    tournamentList: {
        listStyle: 'none',
        padding: 0,
    },
    tournamentItem: {
        marginBottom: '15px',
        padding: '20px',
        border: '1px solid #ddd',
        borderRadius: '5px',
        backgroundColor: '#f9f9f9',
    },
    buttonContainer: {
        display: 'flex',
        justifyContent: 'flex-end',
        marginTop: '10px',
    },
    updateButton: {
        marginRight: '10px',
        padding: '8px 12px',
        backgroundColor: '#28a745',
        color: '#fff',
        textDecoration: 'none',
        borderRadius: '5px',
    },
    deleteButton: {
        padding: '8px 12px',
        backgroundColor: '#dc3545',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
    },
    joinButton: {
        padding: '8px 12px',
        backgroundColor: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
        marginTop: '10px',
    },
    leaveButton: {
        padding: '8px 12px',
        backgroundColor: '#dc3545',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
        marginTop: '10px',
    },
};

export default Tournaments;
