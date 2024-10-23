import React, { useEffect, useState } from 'react';
import API from '../../services/api';

const Leaderboard: React.FC = () => {
    const [leaders, setLeaders] = useState([]);

    useEffect(() => {
        const fetchLeaderboard = async () => {
            try {
                const response = await API.get('/leaderboard');
                setLeaders(response.data);
            } catch (error) {
                console.error('Error fetching leaderboard', error);
            }
        };
        fetchLeaderboard();
    }, []);

    return (
        <div>
            <h2>Leaderboard</h2>
            <ol>
                {leaders.map((leader: any, index: number) => (
                    <li key={index}>
                        {leader.name} - {leader.points}
                    </li>
                ))}
            </ol>
        </div>
    );
};

export default Leaderboard;
