import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
// import { getAuthToken } from '../authUtils'; // Commented out for testing

const CreateMatch: React.FC = () => {
    const { tournamentId } = useParams<{ tournamentId: string }>();
    const navigate = useNavigate();
    const [matches, setMatches] = useState<{ id: number; player1: string; player2: string; status: string }[]>([]);
    const [newMatch, setNewMatch] = useState({ player1: '', player2: '' });

    // Placeholder token for testing
    const token = "mockTokenForTesting"; // Replace with any string for testing

    useEffect(() => {
        fetchMatches();
    }, [tournamentId]);

    // Fetch all matches for the tournament
    const fetchMatches = async () => {
        try {
            const response = await fetch(`http://localhost:8080/matches`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            const data = await response.json();
            setMatches(data);
        } catch (error) {
            console.error('Failed to fetch matches:', error);
        }
    };

    // Create a new match
    const createMatch = async () => {
        const matchDetails = {
            tournamentId,
            player1: newMatch.player1,
            player2: newMatch.player2,
        };
        try {
            const response = await fetch(`http://localhost:8080/matches/create`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(matchDetails),
            });
            if (response.ok) {
                setNewMatch({ player1: '', player2: '' });
                fetchMatches();
            }
        } catch (error) {
            console.error('Error creating match:', error);
        }
    };

    // Delete a match by ID
    const deleteMatch = async (id: number) => {
        try {
            await fetch(`http://localhost:8080/matches/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            fetchMatches(); // Refresh matches after deletion
        } catch (error) {
            console.error('Failed to delete match:', error);
        }
    };

    return (
        <div className="container mt-4">
            <h2>Manage Matches for Tournament</h2>

            <form
                onSubmit={(e) => {
                    e.preventDefault();
                    createMatch();
                }}
                className="form-inline my-3"
            >
                <div className="form-group">
                    <input
                        type="text"
                        className="form-control mx-2"
                        placeholder="Player 1 ID"
                        value={newMatch.player1}
                        onChange={(e) => setNewMatch({ ...newMatch, player1: e.target.value })}
                        required
                    />
                </div>
                <div className="form-group">
                    <input
                        type="text"
                        className="form-control mx-2"
                        placeholder="Player 2 ID"
                        value={newMatch.player2}
                        onChange={(e) => setNewMatch({ ...newMatch, player2: e.target.value })}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary">Create Match</button>
            </form>

            <ul className="list-group mt-4">
                {matches.map((match) => (
                    <li key={match.id} className="list-group-item d-flex justify-content-between align-items-center">
                        {match.player1} vs {match.player2}
                        <button className="btn btn-danger" onClick={() => deleteMatch(match.id)}>
                            Delete
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default CreateMatch;
