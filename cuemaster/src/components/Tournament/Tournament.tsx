import React, { useEffect, useState } from 'react';
import API from '../../services/api';
import { Link } from 'react-router-dom';
import { isAuthenticated, getUserRole } from '../authUtils';

interface Tournament {
    id: number;
    name: string;
    date: string;
    location: string;
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
                        <h3>{tournament.name}</h3>
                        <p><strong>Date:</strong> {new Date(tournament.date).toLocaleDateString()}</p>
                        <p><strong>Location:</strong> {tournament.location}</p>
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
};

export default Tournaments;
