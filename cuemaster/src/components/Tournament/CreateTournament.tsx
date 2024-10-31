import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './CreateTournament.css';
import { getAuthToken } from '../authUtils'; // Import your auth utility

const CreateTournament: React.FC = () => {
    const navigate = useNavigate();
    const [tournamentName, setTournamentName] = useState('');
    const [location, setLocation] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [time, setTime] = useState('');
    const [status, setStatus] = useState('UPCOMING');
    const [description, setDescription] = useState('');
    const [winnerId, setWinnerId] = useState<string | null>(null);
    const [players, setPlayers] = useState<string[]>([]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        const tournamentDetails = {
            tournamentname: tournamentName,
            location,
            startDate,
            endDate,
            time: `${time}:00`, // Formatting time as HH:MM:SS
            status,
            description,
            winnerId: winnerId ? Number(winnerId) : null,
            players: players.map(id => Number(id))
        };

        try {
            const token = getAuthToken(); // Get the auth token
            const response = await fetch('http://localhost:8080/tournaments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // Add the token to the headers
                },
                body: JSON.stringify(tournamentDetails),
            });

            if (response.ok) {
                navigate('/tournaments', { state: { showNewlyCreated: true } }); // Redirect with state
            } else {
                console.error('Failed to create tournament:', response.statusText);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };

    return (
        <div className="container">
            <h1 className="title">Create Tournament</h1>
            <form onSubmit={handleSubmit} className="form">
                <div className="form-group">
                    <label>Tournament Name:</label>
                    <input
                        type="text"
                        value={tournamentName}
                        onChange={(e) => setTournamentName(e.target.value)}
                        required
                        className="input"
                    />
                </div>
                <div className="form-group">
                    <label>Location:</label>
                    <input
                        type="text"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                        required
                        className="input"
                    />
                </div>
                <div className="form-group">
                    <label>Start Date:</label>
                    <input
                        type="date"
                        value={startDate}
                        onChange={(e) => {
                            setStartDate(e.target.value);
                            setEndDate('');
                        }}
                        required
                        className="input"
                        min={new Date().toISOString().split('T')[0]}
                    />
                </div>
                <div className="form-group">
                    <label>End Date:</label>
                    <input
                        type="date"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                        required
                        className="input"
                        min={startDate ? startDate : undefined}
                        disabled={!startDate}
                    />
                </div>
                <div className="form-group">
                    <label>Time:</label>
                    <input
                        type="time"
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                        required
                        className="input"
                    />
                </div>
                <div className="form-group">
                    <label>Status:</label>
                    <select
                        value={status}
                        onChange={(e) => setStatus(e.target.value)}
                        required
                        className="input"
                    >
                        <option value="UPCOMING">Upcoming</option>
                        <option value="ONGOING">Ongoing</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="CANCELLED">Cancelled</option>
                        <option value="ROUND_OF_32">Round of 32</option>
                        <option value="ROUND_OF_16">Round of 16</option>
                        <option value="QUARTER_FINALS">Quarter Finals</option>
                        <option value="SEMI_FINAL">Semi Final</option>
                        <option value="FINAL">Final</option>
                    </select>
                </div>
                <div className="form-group">
                    <label>Description:</label>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        required
                        className="input"
                        style={{ height: '100px' }}
                    />
                </div>
                <div className="form-group">
                    <label>Winner ID:</label>
                    <input
                        type="text"
                        value={winnerId || ''}
                        onChange={(e) => setWinnerId(e.target.value)}
                        className="input"
                    />
                </div>
                <div className="form-group">
                    <label>Player IDs (comma separated):</label>
                    <input
                        type="text"
                        value={players.join(',')}
                        onChange={(e) => setPlayers(e.target.value.split(','))}
                        className="input"
                    />
                </div>
                <button type="submit" className="submit-button">Create</button>
            </form>
        </div>
    );
};

export default CreateTournament;
