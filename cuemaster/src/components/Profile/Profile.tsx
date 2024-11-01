import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import API from '../../services/api';
import axios from 'axios';

// Profile interface definition
interface Profile {
  id: number;
  firstname: string;
  lastname: string;
  birthdate: string;
  birthlocation: string;
  profilephotopath: string;
  organization?: string;
  TournamentCount?: number;
  TournamentWinCount?: number;
  MatchCount?: number;
  MatchWinCount?: number;
  points?: number;
}

const Profile: React.FC = () => {
  const { userId, profileId } = useParams<{ userId: string; profileId: string }>(); // Extracting from URL params (extracting userid and profileid from url)
  const [profile, setProfile] = useState<Profile | null>(null); //store the getprofile to this profile variable 
  const [loading, setLoading] = useState(true);//keep track of whether the profile data is currently being loaded (true - not fetched / false - fetched/error)
  const [error, setError] = useState<string | null>(null); //store any error message 

  useEffect(() => {                 //useEffect perform side effects such as data fetching 
    
    //Constructing the fetchprofile function
    const fetchProfile = async () => {
      try {
        const response = await API.get(`/user/${userId}/profile/${profileId}`);
        setProfile(response.data);
      } catch (err) {
        setError('Failed to load profile');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    //calling the function 
    fetchProfile();
  }, [userId, profileId]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

  // If neither loading nor error, render the profile
  return (
    <div>
      <h1>User Profile</h1>
      {profile && (
        <div>
          <h2>{profile.firstname} {profile.lastname}</h2>
          <p>Date of Birth: {profile.birthdate}</p>
          <p>Birth Location: {profile.birthlocation}</p>
          {profile.organization && <p>Organization: {profile.organization}</p>}
          <p>Tournaments Played: {profile.TournamentCount}</p>
          <p>Tournaments Won: {profile.TournamentWinCount}</p>
          <p>Matches Played: {profile.MatchCount}</p>
          <p>Matches Won: {profile.MatchWinCount}</p>
          <p>Points: {profile.points}</p>
          {profile.profilephotopath && (
            <img src={profile.profilephotopath} alt="Profile" style={{ width: '150px', height: '150px', borderRadius: '8px' }} />
          )}
        </div>
      )}
    </div>
  );
};

export default Profile;