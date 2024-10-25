import React, { useEffect, useState } from 'react';
import API from '../../services/api';

// Define a type for the leaderboard item
interface Leader {
    name: string;
    points: number;
}

const Leaderboard: React.FC = () => {
    const [leaders, setLeaders] = useState<Leader[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLeaderboard = async () => {
            try {
                const response = await API.get<Leader[]>('/leaderboard'); // Specify the expected type here
                setLeaders(response.data); // TypeScript now knows response.data is of type Leader[]
            } catch (error) {
                console.error('Error fetching leaderboard', error);
                setError('Failed to fetch leaderboard. Please try again later.');
            } finally {
                setLoading(false); // Set loading to false after the fetch is complete
            }
        };

        fetchLeaderboard();
    }, []);

    if (loading) {
        return <div>Loading...</div>; // Loading state
    }

    if (error) {
        return <div>{error}</div>; // Error message
    }

    return (
        <div>
            <h2>Leaderboard</h2>
            <ol>
                {leaders.map((leader, index) => (
                    <li key={index}>
                        {leader.name} - {leader.points}
                    </li>
                ))}
            </ol>
        </div>
    );
};

export default Leaderboard;
