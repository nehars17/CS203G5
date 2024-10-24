import React, { useEffect, useState } from 'react';
import API from '../../services/api';

const Matches: React.FC = () => {
    const [matches, setMatches] = useState([]);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await API.get('/matches');
                setMatches(response.data);
            } catch (error) {
                console.error('Error fetching matches', error);
            }
        };
        fetchMatches();
    }, []);

    return (
        <div>
            <h2>Tournament Matches</h2>
            <ul>
                {matches.map((match: any, index: number) => (
                    <li key={index}>
                        {match.player1} vs {match.player2} - {match.date}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Matches;
