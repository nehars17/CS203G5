import React, { useEffect, useState } from 'react';
import API from '../../services/api';
import './ProfileDashboard.css';

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
        response = await API.get<Profile[]>(`/profiles`); // Directly expect Profile[]
      } else {
        response = await API.get<Profile[]>(`/profiles?role=${role}`); // Directly expect Profile[]
      }

      console.log('Response from API:', response); // Log the entire response
      console.log('Profiles data:', response.data); // Log only the data
      setProfiles(response.data); // Now correctly typed as Profile[]
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
      <h1 className="dashboard-title">Profile Dashboard</h1>

      {/* Filter buttons */}
      <div className="button-group">
        <button className="filterbutton" onClick={() => handleFilterChange('All')}>All</button>
        <button className="filterbutton" onClick={() => handleFilterChange('Player')}>Players</button>
        <button className="filterbutton" onClick={() => handleFilterChange('Organizer')}>Organizers</button>
      </div>

      {/* Loading state */}
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
                  {/* Profile photo */}
                  <img
                    src={imageUrl}
                    alt={`${profile.firstname} ${profile.lastname}`}
                    className="profile-photo"
                  />

                  {/* Profile name */}
                  <h3 className="profile-name">{profile.firstname} {profile.lastname}</h3>

                  {/* View profile button */}
                  <button
                    className="viewbutton"
                    onClick={() => window.location.href = `/profile/${profile.user.id}`}
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