import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './CreateTournament.css'; // Import the CSS file

const CreateTournament: React.FC = () => {
    const navigate = useNavigate();
    const [tournamentName, setTournamentName] = useState('');
    const [location, setLocation] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    // const [time, setTime] = useState('');
    const [status, setStatus] = useState('UPCOMING'); // Use uppercase to match typical enum style
    const [description, setDescription] = useState('');

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
    
        // Format the time to "HH:MM:SS"
        // const formattedTime = `${time}:00`;
    
        const tournamentDetails = {
            tournamentName,
            location,
            startDate,
            endDate,
            // time: formattedTime, // Use the formatted time as LocalTime format
            status, // Make sure the status matches one of the expected values in the backend
            description,
        };
    
        try {
            const response = await fetch('http://localhost:8080/tournaments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(tournamentDetails),
            });
    
            if (response.ok) {
                navigate('/tournaments');
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
                            setEndDate(''); // Clear end date when start date changes
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
                {/* <div className="form-group">
                    <label>Time:</label>
                    <input
                        type="time"
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                        required
                        className="input"
                    />
                </div> */}
                <div className="form-group">
                    <label>Status:</label>
                    <select
                        value={status}
                        onChange={(e) => setStatus(e.target.value.toUpperCase())}
                        required
                        className="input"
                    >
                        <option value="UPCOMING">Upcoming</option>
                        <option value="ONGOING">Ongoing</option>
                        <option value="COMPLETED">Completed</option>
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
                <button type="submit" className="submit-button">Create</button>
            </form>
        </div>
    );
};

export default CreateTournament;