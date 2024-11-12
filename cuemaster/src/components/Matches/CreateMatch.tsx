import React from 'react';
import { getAuthToken } from '../authUtils';

interface CreateMatchProps {
    tournamentId: string;
    refetchMatches: () => void;
}

const CreateMatch: React.FC<CreateMatchProps> = ({ tournamentId, refetchMatches }) => {
    // Generate Matches - Send request to backend to generate matches
    const generateMatches = async () => {
        try {
            const token = getAuthToken();
            const response = await fetch(`http://localhost:8080/matches/tournament/${tournamentId}`, {
                method: 'POST', // Assuming POST method for generating matches
                headers: { 'Authorization': `Bearer ${token}` },
            });

            if (response.ok) {
                refetchMatches(); // Refetch matches after generating
            } else {
                console.error('Failed to generate matches:', response.statusText);
            }
        } catch (error) {
            console.error('Error generating matches:', error);
        }
    };

    return (
        <div className="text-center mb-4">
            <button onClick={generateMatches} className="btn btn-success">
                Generate Matches
            </button>
        </div>
    );
};

export default CreateMatch;
