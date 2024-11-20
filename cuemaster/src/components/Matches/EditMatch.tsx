// EditMatch.tsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Form } from 'react-bootstrap';

const EditMatch: React.FC = () => {
    const { matchId } = useParams<{ matchId: string }>();
    const navigate = useNavigate();
    const [player1Score, setPlayer1Score] = useState('');
    const [player2Score, setPlayer2Score] = useState('');

    useEffect(() => {
        // Fetch match data by matchId
    }, [matchId]);

    const handleSave = async () => {
        // Implement save functionality for the match
        navigate(-1); // Go back to the previous page
    };

    return (
        <div className="container mt-4">
            <h2>Edit Match</h2>
            <Form>
                <Form.Group controlId="player1Score">
                    <Form.Label>Player 1 Score</Form.Label>
                    <Form.Control
                        type="text"
                        value={player1Score}
                        onChange={(e) => setPlayer1Score(e.target.value)}
                    />
                </Form.Group>
                <Form.Group controlId="player2Score" className="mt-3">
                    <Form.Label>Player 2 Score</Form.Label>
                    <Form.Control
                        type="text"
                        value={player2Score}
                        onChange={(e) => setPlayer2Score(e.target.value)}
                    />
                </Form.Group>
                <Button variant="primary" className="mt-3" onClick={handleSave}>
                    Save
                </Button>
            </Form>
        </div>
    );
};

export default EditMatch;
