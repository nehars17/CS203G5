import React, { useEffect, useState } from 'react';
import API from '../../services/api';
import './ProfileDashboard.css';

interface User{
  id: number;
  username: string;
}

interface Profile {
  id: number; // Change to number if profile IDs are integers
  firstname: string;
  lastname: string;
  birthdate: string; // If you want to access it in the frontend
  birthlocation: string;
  profilephotopath: string; // Ensure this field is included
  organization: string | null; // Allow null if it's optional
  points: number | null; // Allow null if it's optional
  user: User; // Nested user object
  tournamentCount: number | null; 
  matchCount: number | null;
  matchWinCount: number | null;
  tournamentWinCount: number | null;
}

const ProfileDashboard: React.FC = () => {
  const [profiles, setProfiles] = useState<Profile[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'All' | 'Player' | 'Organizer'>('All');

  const handleFilterChange = async (role: 'All' | 'Player' | 'Organizer') => {
    setLoading(true);
    try {
      let response;

      // Handle API calls based on the selected role
      if (role === "All") {
        response = await API.get(`/profiles`);
      } else {
        response = await API.get(`/profiles?role=${role}`);
      }

      console.log('Response from API:', response); // Log the entire response
      console.log('Profiles data:', response.data); // Log only the data
      setProfiles(response.data);
      setFilter(role);
    } catch (error) {
      console.error(`Failed to fetch ${role} profiles:`, error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    handleFilterChange('All');
  }, []);

  return (
    <div className="dashboard-container">
      <h1>Profile Dashboard</h1>
      <div className="button-group">
        <button className="button" onClick={() => handleFilterChange('All')}>All</button>
        <button className="button" onClick={() => handleFilterChange('Player')}>Players</button>
        <button className="button" onClick={() => handleFilterChange('Organizer')}>Organizers</button>
      </div>

      {loading ? (
        <p className="loading-message">Loading profiles...</p>
      ) : (
        <div className="profiles-grid">
          {profiles.map((profile) => {
            const imageUrl = `http://localhost:8080/profilePhotos/${profile.profilephotopath}`;
            console.log(`Image URL: ${imageUrl}`); // Log the image URL

            return (
              <div key={profile.id} className="profile-card">
                <div className="profile-content">
                  <img
                    src={imageUrl}
                    alt={`${profile.firstname} ${profile.lastname}`}
                    className="profile-photo"
                  />
                  <h3>{profile.firstname} {profile.lastname}</h3>
                  <button
                    className="button"
                    onClick={() => (window.location.href = `/user/${profile.id}/profile`)}
                  >
                    View Profile
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};



export default ProfileDashboard;