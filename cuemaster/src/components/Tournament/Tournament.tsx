import React, { useEffect, useState } from 'react';
import API from '../../services/api';
import { Link } from 'react-router-dom';
import { isAuthenticated, getUserRole, getUserIdFromToken, getAuthToken } from '../authUtils';

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
            const token = getAuthToken();
            const response = await fetch(`http://localhost:8080/tournaments/${id}/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(playerId),  // Send playerId as a plain number
            });
    
            if (response.ok) {
                console.log(`Player ${playerId} joined tournament ${id}.`);
                alert(`You have successfully joined!`);

                // Fetch updated tournament data (as previously described)
                const updatedResponse = await fetch(`http://localhost:8080/tournaments/${id}`, {
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
                console.error('Failed to join tournament:', response.statusText);
            }
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
            const token = getAuthToken();
            const response = await fetch(`http://localhost:8080/tournaments/${id}/leave`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(playerId),  // Send playerId as a plain number
            });
    
            if (response.ok) {
                console.log(`Player ${playerId} left tournament ${id}.`);
                alert(`You have successfully left!`);

                // Fetch updated tournament data to update state
                const updatedResponse = await fetch(`http://localhost:8080/tournaments/${id}`, {
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
                console.error('Failed to leave tournament:', response.statusText);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };
    

    const handleDelete = async (id: number) => {
        if (window.confirm('Are you sure you want to delete this tournament?')) {
            try {
                const token = getAuthToken();
                const response = await fetch(`http://localhost:8080/tournaments/${id}`, {
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

            {/* Season Dropdown */}
            <select value={seasonFilter} onChange={(e) => setSeasonFilter(e.target.value)} style={styles.dropdown}>
                <option value="ALL">All Seasons</option>
                <option value="Season 1">Season 1</option>
                <option value="Season 2">Season 2</option>
                <option value="Season 3">Season 3</option>
                <option value="Season 4">Season 4</option>
            </select>

            {/* Show Create Tournament button only for authenticated organizers */}
            {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                <Link to="/tournaments/create-tournament" className="create-button" style={styles.createButton}>
                    Create Tournament
                </Link>
            )}

            <ul style={styles.tournamentList}>
                {filteredTournaments.map((tournament) => (  // Use filteredTournaments here
                    <li
                    key={tournament.id}
                    style={styles.tournamentItem}
                    onMouseEnter={(e) => e.currentTarget.style.boxShadow = styles.tournamentItemHover.boxShadow}
                    onMouseLeave={(e) => e.currentTarget.style.boxShadow = styles.tournamentItem.boxShadow}
                    >
                        <h3>{tournament.tournamentname}</h3>
                        <p><strong>Date:</strong> {tournament.startDate} to {tournament.endDate}</p>
                        <p><strong>Time:</strong> {tournament.time}</p>
                        <p><strong>Location:</strong> {tournament.location}</p>
                        <p><strong>Status:</strong> {tournament.status}</p>
                        <p><strong>Description:</strong> {tournament.description}</p>
                        <p><strong>WinnerID:</strong> {tournament.winnerId}</p>
                        {/* Render player list only for organizers */}
                        {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                            <p><strong>Players (ID):</strong> {tournament.players.length > 0 ? tournament.players.join(', ') : 'No players'}</p>
                        )}
                        

                        {/* Render Join button only for authenticated players not already in the tournament */}
                        {isUserAuthenticated && tournament.players.length < 32 && userRole === "ROLE_PLAYER" && playerId !== null && !tournament.players.includes(playerId) && tournament.status === "UPCOMING" && (
                            <button onClick={() => handleJoin(tournament.id)} style={styles.joinButton}>
                                Join
                            </button>
                        )}

                        {/* Render Leave button only for authenticated players in the tournament */}
                        {isUserAuthenticated && userRole === "ROLE_PLAYER" && playerId !== null && tournament.players.includes(playerId) && tournament.status === "UPCOMING" && (
                            <button onClick={() => handleLeave(tournament.id)} style={styles.leaveButton}>
                                Leave
                            </button>
                        )}



                        {/* Render buttons only for authenticated organizers */}
                        {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                            <div style={styles.buttonContainer}>
                                <Link to={`/tournaments/update-tournament/${tournament.id}`} className="update-button" style={styles.updateButton}>
                                    Edit
                                </Link>
                                <button
                                    onClick={() => handleDelete(tournament.id)}
                                    style={styles.deleteButton}
                                >
                                    Delete
                                </button>
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
        fontSize: '2.5rem',
        fontWeight: 'bold',
        color: '#333',
        marginBottom: '20px',
    },
    tabs: {
        display: 'flex',
        justifyContent: 'center',
        gap: '10px',
        marginBottom: '20px',
    },
    tab: {
        padding: '10px 20px',
        border: '1px solid #ddd',
        borderRadius: '5px',
        backgroundColor: '#e0e0e0',
        cursor: 'pointer',
        fontWeight: 'bold',
        color: '#555',
        transition: 'background-color 0.3s, color 0.3s',
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
        fontWeight: 'bold',
        transition: 'background-color 0.3s',
        cursor: 'pointer',
    },
    dropdown: {
        margin: '20px', 
        padding: '10px', 
        fontSize: '1rem' 
    },
    tournamentList: {
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)', // Ensures 3 items per row on larger screens
        gap: '20px',
        listStyle: 'none',
        padding: 0,
    },
    tournamentItem: {
        padding: '20px',
        border: '1px solid #ddd',
        borderRadius: '8px',
        backgroundColor: '#fff',
        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
        transition: 'box-shadow 0.3s',
        textAlign: 'left' as const, // Align details to the left
    },
    tournamentItemHover: {
        boxShadow: '0 8px 16px rgba(0, 0, 0, 0.15)',
    },
    tournamentHeading: {
        fontSize: '1.5rem',
        fontWeight: 'bold',
        marginBottom: '10px',
        color: '#333',
        textAlign: 'center' as const, // Center-align the tournament name
    },
    tournamentDetails: {
        fontSize: '1rem',
        color: '#555',
        marginBottom: '8px',
    },
    buttonContainer: {
        display: 'flex',
        justifyContent: 'space-between',
        marginTop: '15px',
    },
    updateButton: {
        padding: '8px 12px',
        backgroundColor: '#28a745',
        color: '#fff',
        textDecoration: 'none',
        borderRadius: '5px',
        fontWeight: 'bold',
        cursor: 'pointer',
        transition: 'background-color 0.3s',
    },
    deleteButton: {
        padding: '8px 12px',
        backgroundColor: '#dc3545',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        fontWeight: 'bold',
        cursor: 'pointer',
        transition: 'background-color 0.3s',
        textDecoration: 'none', // Remove underline
    },
    joinButton: {
        padding: '8px 12px',
        backgroundColor: '#007bff',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        fontWeight: 'bold',
        cursor: 'pointer',
        transition: 'background-color 0.3s',
        marginTop: '10px',
    },
    leaveButton: {
        padding: '8px 12px',
        backgroundColor: '#ff6b6b',
        color: '#fff',
        border: 'none',
        borderRadius: '5px',
        fontWeight: 'bold',
        cursor: 'pointer',
        transition: 'background-color 0.3s',
        marginTop: '10px',
    },

};

export default Tournaments;
