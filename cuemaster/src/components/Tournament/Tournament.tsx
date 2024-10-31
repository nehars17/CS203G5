import React, { useEffect, useState } from 'react';
import API, { deleteTournament } from '../../services/api';
import { Link } from 'react-router-dom';
import { isAuthenticated, getUserRole } from '../authUtils';

interface Tournament {
    id: number;
    tournamentname: string;
    startDate: string;
    endDate: string;
    time: string;
    location: string;
    status: string;
    description: string;
}

const Tournaments: React.FC = () => {
    const [tournaments, setTournaments] = useState<Tournament[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const isUserAuthenticated = isAuthenticated();
    const userRole = getUserRole();

    useEffect(() => {
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

        fetchTournaments();
    }, []);

    const handleDelete = async (id: number) => {
        if (window.confirm('Are you sure you want to delete this tournament?')) {
            try {
                await deleteTournament(id);
                // Update state to remove the deleted tournament from the list
                setTournaments(prevTournaments => prevTournaments.filter(tournament => tournament.id !== id));
                console.log(`Tournament with id ${id} deleted from frontend state.`); // Log for confirmation
            } catch (error) {
                console.error('Error deleting tournament:', error);
                setError('Failed to delete tournament. Please try again later.');
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
            <h1 style={styles.title}>All Tournaments</h1>

            {/* Show Create Tournament button only for authenticated organizers */}
            {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                <Link to="/tournaments/create-tournament" className="create-button" style={styles.createButton}>
                    Create Tournament
                </Link>
            )}
            
            <ul style={styles.tournamentList}>
                {tournaments.map((tournament) => (
                    <li key={tournament.id} style={styles.tournamentItem}>
                        <h3>{tournament.tournamentname}</h3>
                        <p><strong>Date:</strong> {tournament.startDate} to {tournament.endDate}</p>
                        <p><strong>Time:</strong> {tournament.time}</p>
                        <p><strong>Location:</strong> {tournament.location}</p>
                        <p><strong>Status:</strong> {tournament.status}</p>
                        <p><strong>Description:</strong> {tournament.description}</p>

                        {/* Render buttons only for authenticated organizers */}
                        {isUserAuthenticated && userRole === "ROLE_ORGANISER" && (
                            <div style={styles.buttonContainer}>
                                <Link to={`/tournaments/update-tournament/${tournament.id}`} className="update-button" style={styles.updateButton}>
                                    Update
                                </Link>
                                <button onClick={() => handleDelete(tournament.id)} style={styles.deleteButton}>
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
        fontSize: '2rem',
        marginBottom: '20px',
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
};

export default Tournaments;
