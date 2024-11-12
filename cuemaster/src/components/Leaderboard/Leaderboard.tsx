import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Leaderboard.css';
import config from '../../config';
interface User {
    id: number;
    username: string;
}

interface Profile {
    id: number;
    firstname: string;
    lastname: string;
    birthdate: string;
    birthlocation: string;
    profilephotopath: string;
    organization: string | null;
    points: number | null;
    user: User;
    tournamentCount: number | null;
    matchCount: number | null;
    matchWinCount: number | null;
    tournamentWinCount: number | null;
}

const Leaderboard = () => {
    const [profiles, setProfiles] = useState<Profile[]>([]);
    const [ranks, setRanks] = useState<{ [key: number]: number }>({});
    const navigate = useNavigate();


    useEffect(() => {
        // Fetch sorted profiles
        fetch(`${config.apiBaseUrl}/leaderboard`)
            .then(response => response.json())
            .then(data => {
                setProfiles(data);
                console.log("Fetched profiles:", data);
            })
            .catch(error => console.error('Error fetching leaderboard:', error));

        // Fetch ranks
        fetch(`${config.apiBaseUrl}/playerrank`)
            .then(response => response.json())
            .then(data => {
                setRanks(data);
                console.log("Fetched ranks:", data);
            })
            .catch(error => console.error('Error fetching profile ranks:', error));
    }, []);

    // Get the top 3 profiles based on ranks
    const top3Profiles = profiles
        .map((profile) => ({
            ...profile,
            rank: ranks[profile.user.id] ?? Infinity, // Ensure undefined ranks are treated as the lowest rank
        }))
        .sort((a, b) => a.rank - b.rank) // Sort by rank
        .slice(0, 3); // Take only top 3

    // Get the rest of the profiles (excluding top 3)
    const restOfProfiles = profiles.filter(
        (profile) => !top3Profiles.some((topProfile) => topProfile.id === profile.id)
    );

    // Handle click on profile container to view the profile details
    const handleProfileClick = (userId: number) => {
        navigate(`/profile/${userId}`); // Navigate to profile page using userId
    };

    return (
        <div className="leaderboard">
            <h2>Leaderboard</h2>
            <div className="top-3-profiles">
                {top3Profiles.map((profile, index) => (
                    <div
                        className={`top-profile top-profile-${index + 1}`} 
                        key={profile.id}
                        onClick={() => handleProfileClick(profile.user.id)} // Make the entire container clickable
                    >
                        {/* Profile photo at the top */}
                        <div className="profile-leaderboard-top3-photo">
                            <img
                                src={`${config.apiBaseUrl}/profilePhotos/${profile.profilephotopath}`}
                                alt={`${profile.firstname} ${profile.lastname}`}
                                className="profile-leaderboard-photo"
                            />
                        </div>
    
                        {/* Rank and Full Name beside each other */}
                        <div className="profile-leaderboard-top3-info">
                            <span className="rank">[{index + 1}]</span>
                            <span className="full-name">{profile.firstname} {profile.lastname}</span>
                        </div>
    
                        {/* Points below Full Name */}
                        <div className="profile-leaderboard-top3-info">
                            <span className="points">{profile.points !== null ? profile.points : 'N/A'}</span>
                        </div>
                    </div>
                ))}
            </div>
    
            {/* Display rest of the profiles */}
            <table className="leaderboard-table">
                <thead>
                    <tr>
                        <th>Rank</th>
                        <th>ProfilePhoto</th>
                        <th>Full Name</th>
                        <th>Points</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    {restOfProfiles.map((profile) => (
                        <tr key={profile.id}>
                            <td>{ranks[profile.user.id] !== undefined ? ranks[profile.user.id] : 'N/A'}</td>
                            <td>
                                <img
                                    src={`${config.apiBaseUrl}/profilePhotos/${profile.profilephotopath}`}
                                    alt={`${profile.firstname} ${profile.lastname}`}
                                    className="profile-leaderboard-photo"
                                />
                            </td>
                            <td>{profile.firstname} {profile.lastname}</td>
                            <td>{profile.points !== null ? profile.points : 'N/A'}</td>
                            <td>
                                <button onClick={() => handleProfileClick(profile.user.id)}>View</button> {/* Add View button */}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default Leaderboard;