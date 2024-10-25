import React, { useEffect, useState } from 'react';
import API from '../../services/api';

// Define a type for the match item
interface Match {
    player1: string;
    player2: string;
    date: string; // Adjust types as necessary, e.g., Date if using Date objects
}

const Matches: React.FC = () => {
    const [matches, setMatches] = useState<Match[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await API.get<Match[]>('/matches'); // Specify the expected type here
                setMatches(response.data);
            } catch (error) {
                console.error('Error fetching matches', error);
                setError('Failed to fetch matches. Please try again later.');
            } finally {
                setLoading(false); // Set loading to false after the fetch is complete
            }
        };

        fetchMatches();
    }, []);

    if (loading) {
        return <div>Loading...</div>; // Loading state
    }

    if (error) {
        return <div>{error}</div>; // Error message
    }

    return (
        <div>
            <h2>Tournament Matches</h2>
            <ul>
                {matches.map((match, index) => (
                    <li key={index}>
                        {match.player1} vs {match.player2} - {match.date}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Matches;
