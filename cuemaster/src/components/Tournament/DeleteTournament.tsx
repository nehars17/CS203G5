import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import './CreateTournament.css';
import { getAuthToken } from '../authUtils';

const DeleteTournament: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [tournamentName, setTournamentName] = useState('');
    const [error, setError] = useState<string | null>(null);

    // Fetch tournament details when the component mounts
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            if (!id) {
                console.error("Tournament ID is undefined.");
                return;
            }

            try {
                const token = getAuthToken();
                const response = await fetch(`http://localhost:8080/tournaments/${id}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.ok) {
                    const tournament = await response.json();
                    setTournamentName(tournament.tournamentname);
                } else {
                    console.error('Failed to fetch tournament details:', response.statusText);
                    setError('Failed to fetch tournament details.');
                }
            } catch (error) {
                console.error('Error:', error);
                setError('An error occurred while fetching tournament details.');
            }
        };

        fetchTournamentDetails();
    }, [id]);

    const handleDelete = async () => {
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
                    // Redirect back to the tournaments page
                    navigate('/tournaments');
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

    return (
        <div className="container">
            <h1 className="title">Delete Tournament</h1>
            {error && <p className="error">{error}</p>}
            <div>
                <p>Are you sure you want to delete the tournament: {tournamentName}?</p>
                <button onClick={handleDelete} className="delete-button">Delete Tournament</button>
                <button onClick={() => navigate('/tournaments')} className="cancel-button">Cancel</button>
            </div>
        </div>
    );
};

export default DeleteTournament;
