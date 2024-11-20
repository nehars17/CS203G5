import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import API from '../../services/api';
import './Profile.css';
import { isAuthenticated, getUserIdFromToken, getUserRole } from 'cuemaster/src/components/authUtils';
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

const Profile: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await API.get<Profile>(
          `${config.apiBaseUrl}/profile/${Number(userId)}`
        );
        setProfile(response.data);
      } catch (err) {
        if (err && typeof err === 'object' && 'response' in err) {
          const errorMessage = (err as any).response?.data?.message || 'Failed to load profile';
          setError(errorMessage);
        } else {
          setError('An unexpected error occurred.');
        }
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [userId]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

  const canEdit = isAuthenticated() && (getUserIdFromToken() === Number(userId) || getUserRole() === 'ROLE_ADMIN');

  return (
    <div className="profile-container">
      {profile && (
        <>
          <div className="profile-info">
            <div className="profile-image">
              {profile.profilephotopath && (
                <img
                  src={`${config.apiBaseUrl}/profilePhotos/${profile.profilephotopath}`}
                  alt="Profile Picture"
                  className="profile-pphoto"
                />
              )}
              <h1 className="Name-info">{profile.firstname} {profile.lastname}</h1>
            </div>

            <div className="profile-details-container">
              <div className="profile-details">
                <p className="profile-details-row"><strong>Date of Birth:</strong> <span className='profile-spaces'> {profile.birthdate} </span></p>
                <p className="profile-details-row"><strong>Place of Birth:</strong> <span className='profile-spaces'> {profile.birthlocation} </span></p>
                {profile.organization && <p className="profile-details-row"><strong>Organization:</strong><span className='profile-spaces'>{profile.organization}</span> </p>}
                {profile.tournamentCount !== null && <p className="profile-details-row"><strong>Tournaments Played:</strong> <span className='profile-spaces'>{profile.tournamentCount}</span></p>}
                {profile.tournamentWinCount !== null && <p className="profile-details-row"><strong>Tournaments Won:</strong> <span className='profile-spaces'>{profile.tournamentWinCount}</span></p>}
                {profile.matchCount !== null && <p className="profile-details-row"><strong>Matches Played:</strong><span className='profile-spaces'> {profile.matchCount}</span></p>}
                {profile.matchWinCount !== null && <p className="profile-details-row"><strong>Matches Won:</strong> <span className='profile-spaces'>{profile.matchWinCount}</span></p>}
                {profile.points !== null && <p className="profile-details-row"><strong>Points:</strong><span className='profile-spaces'> {profile.points} </span></p>}
              </div>
            </div>
          </div>

          {canEdit && (
            <div className="edit-button-container">
              <button
                className="editbutton"
                onClick={() => (window.location.href = `/ProfileAmendment/${profile.user.id}`)}
              >
                Edit Profile
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default Profile;
