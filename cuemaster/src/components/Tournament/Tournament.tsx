import React, { useEffect, useState } from 'react';
import API from '../../services/api';

interface Tournament {
    id: number;
    name: string;
    date: string;
    location: string;
}

const Tournaments: React.FC = () => {
    const [tournaments, setTournaments] = useState<Tournament[]>([]);

    useEffect(() => {
        const fetchTournaments = async () => {
            try {
                const response = await API.get('/tournaments');  // Assuming this is your backend API endpoint
                setTournaments(response.data);
            } catch (error) {
                console.error('Error fetching tournaments', error);
            }
        };
        fetchTournaments();
    }, []);

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>All Tournaments</h1>
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
