import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import './CreateTournament.css';
import { getAuthToken } from '../authUtils';
import config from '../../config';

const UpdateTournament: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [tournamentName, setTournamentName] = useState('');
    const [location, setLocation] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [time, setTime] = useState('');
    const [status, setStatus] = useState('UPCOMING');
    const [description, setDescription] = useState('');
    const [winnerId, setWinnerId] = useState<string | null>(null);
    const [players, setPlayers] = useState<string[]>([]);
    const [matches, setMatches] = useState<{
        status: string; player1: string; player2: string
    }[]>([]);

    // Fetch tournament details when the component mounts
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            if (!id) {
                console.error("Tournament ID is undefined.");
                return;
            }

            try {
                const token = localStorage.getItem('token');
                const response = await fetch(`${config.apiBaseUrl}/tournaments/${id}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                if (response.ok) {
                    const tournament = await response.json();
                    setTournamentName(tournament.tournamentname);
                    setLocation(tournament.location);
                    setStartDate(tournament.startDate);
                    setEndDate(tournament.endDate);
                    setTime(tournament.time.split(':')[0]);
                    setStatus(tournament.status);
                    setDescription(tournament.description);
                    setWinnerId(tournament.winnerId ? String(tournament.winnerId) : null);
                    setPlayers(tournament.players ? tournament.players.map(String) : []);
                } else {
                    console.error('Failed to fetch tournament details:', response.statusText);
                }
            } catch (error) {
                console.error('Error:', error);
            }
        };

        fetchTournamentDetails();
    }, [id]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        const tournamentDetails = {
            tournamentname: tournamentName,
            location,
            startDate,
            endDate,
            time: `${time}:00`,
            status,
            description,
            winnerId: winnerId ? Number(winnerId) : null,
            players: players.map(id => Number(id)),
        };

        try {
            const token = localStorage.getItem('token');
            const response = await fetch(`${config.apiBaseUrl}/tournaments/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(tournamentDetails),
            });

            if (response.ok) {
                console.log(tournamentDetails);
                navigate('/tournaments', { state: { showUpdatedTournament: true } });
            } else {
                console.error('Failed to update tournament:', response.statusText);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };
    // Navigate to the TournamentMatches page
    const navigateToMatches = () => {
        navigate(`/matches/tournament/${id}`);
    };

    return (
        <div className="container mt-4">
            <h1 className="title mb-4">Update Tournament</h1>
            <form onSubmit={handleSubmit} className="form">
                <div className="form-group mb-3">
                    <label htmlFor="tournamentName" className="form-label">Tournament Name:</label>
                    <input
                        type="text"
                        id="tournamentName"
                        value={tournamentName}
                        onChange={(e) => setTournamentName(e.target.value)}
                        required
                        className="form-control"
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="location" className="form-label">Location:</label>
                    <input
                        type="text"
                        id="location"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                        required
                        className="form-control"
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="startDate" className="form-label">Start Date:</label>
                    <input
                        type="date"
                        id="startDate"
                        value={startDate}
                        onChange={(e) => {
                            setStartDate(e.target.value);
                            setEndDate('');
                        }}
                        required
                        className="form-control"
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="endDate" className="form-label">End Date:</label>
                    <input
                        type="date"
                        id="endDate"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                        required
                        className="form-control"
                        min={startDate ? startDate : undefined}
                        disabled={!startDate}
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="time" className="form-label">Time:</label>
                    <input
                        type="time"
                        id="time"
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                        required
                        className="form-control"
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="status" className="form-label">Status:</label>
                    <select
                        id="status"
                        value={status}
                        onChange={(e) => setStatus(e.target.value)}
                        required
                        className="form-select"
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

                <div className="form-group mb-3">
                    <label htmlFor="description" className="form-label">Description:</label>
                    <textarea
                        id="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        required
                        className="form-control"
                        style={{ height: '100px' }}
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="winnerId" className="form-label">Winner ID:</label>
                    <input
                        type="text"
                        id="winnerId"
                        value={winnerId || ''}
                        onChange={(e) => setWinnerId(e.target.value)}
                        className="form-control"
                    />
                </div>

                <div className="form-group mb-3">
                    <label htmlFor="players" className="form-label">Player IDs (comma separated):</label>
                    <input
                        type="text"
                        id="players"
                        value={players.join(',')}
                        onChange={(e) => setPlayers(e.target.value.split(','))}
                        className="form-control"
                    />
                </div>

                <button type="submit" className="btn btn-primary">Update</button>
            </form>

            <div className="center-button">
                <Link to={`/matches/tournament/?id=${id}`} className="create-button">
                    View Matches
                </Link>
            </div>

        </div>
    );
};

export default UpdateTournament;
