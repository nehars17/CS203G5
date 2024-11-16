import React, { useEffect, useState } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import API from '../../services/api';
import './EditProfile.css';
import { isAuthenticated, getUserIdFromToken, getUserRole } from 'cuemaster/src/components/authUtils';
import { useNavigate } from 'react-router-dom';
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

const EditProfile: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newProfilePhoto, setNewProfilePhoto] = useState<File | null>(null);
  const [initialProfile, setInitialProfile] = useState<Profile | null>(null); // To track the initial profile data
  const token = localStorage.getItem('token');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await API.get<Profile>(
          `${config.apiBaseUrl}/profile/${Number(userId)}`
        );
        setProfile(response.data);
        setInitialProfile(response.data);
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

  const handleChangeProfilePhoto = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.style.display = 'none';
    input.onchange = async (e: any) => {
      const file = e.target.files[0]; // Store the selected file

      if (file) {
        // Create a preview URL using FileReader API
        const reader = new FileReader();

        reader.onloadend = () => {
          // Set the selected file to state
          setNewProfilePhoto(file);

          // Set the profile photo path as the preview URL in state
          setProfile((prevProfile) => ({
            ...prevProfile!,
            profilephotopath: reader.result as string, // This is the preview URL
          }));
        };

        // Read the file as a data URL (which is the preview URL)
        reader.readAsDataURL(file);
      }
    };

    // Trigger file input click
    document.body.appendChild(input);
    input.click();
    document.body.removeChild(input);
  };

  const hasUnsavedChanges = () => {
    if (!profile || !initialProfile) return false;
    // Compare each field to detect changes
    return (
      profile.firstname !== initialProfile.firstname ||
      profile.lastname !== initialProfile.lastname ||
      profile.birthdate !== initialProfile.birthdate ||
      profile.birthlocation !== initialProfile.birthlocation ||
      profile.profilephotopath !== initialProfile.profilephotopath ||
      profile.organization !== initialProfile.organization ||
      profile.tournamentCount !== initialProfile.tournamentCount ||
      profile.tournamentWinCount !== initialProfile.tournamentWinCount ||
      profile.matchCount !== initialProfile.matchCount ||
      profile.matchWinCount !== initialProfile.matchWinCount ||
      profile.points !== initialProfile.points
    );
  };

  const handleCancel = () => {
    if (hasUnsavedChanges()) {
      const userConfirmed = window.confirm('You have unsaved changes. Are you sure you want to discard them?');
      if (userConfirmed) {
        navigate(`/profile/${userId}`); // Navigate back to the profile page if confirmed
      }
    } else {
      navigate(`/profile/${userId}`); // Navigate back without prompting if there are no unsaved changes
    }
  };

  const canEdit = isAuthenticated() && (getUserIdFromToken() === Number(userId) || getUserRole() === 'ROLE_ADMIN');
  
  const handleSave = async () => {
    const formData = new FormData();

    // Append profile data as JSON (profile object)
    if (profile) {
      formData.append('profile', new Blob([JSON.stringify(profile)], { type: 'application/json' }));
    }

    // Always append a profilePhoto, even if it's unchanged (pass a placeholder if no new photo)
    if (newProfilePhoto) {
      formData.append('profilePhoto', newProfilePhoto);
    } else {
      // If no new profile photo, append an empty file or null
      formData.append('profilePhoto', new Blob([], { type: 'application/octet-stream' }));
    }

    try {
      const updatedProfileResponse = await fetch(`${config.apiBaseUrl}/user/${userId}/profile`, {
        method: 'PUT',
        body: formData,
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!updatedProfileResponse.ok) {
        throw new Error('Failed to update profile');
      }

      const updatedProfile: Profile = await updatedProfileResponse.json();
      setProfile(updatedProfile);

      alert('Profile updated successfully');
      navigate(`/profile/${userId}`);
    } catch (err) {
      alert('Error updating profile');
      console.error(err);
    }
  };

  return (
    <div className="profile-edit-container">
      {profile && (
        <>
          <div className="profile-edit-info">
            <div className="profile-edit-image-container" onClick={handleChangeProfilePhoto}>
              {/* Show the preview image if available */}
              {profile.profilephotopath && typeof profile.profilephotopath === 'string' && profile.profilephotopath.startsWith('data:image') ? (
                <img
                  src={profile.profilephotopath}  // Use the preview URL here
                  alt="Profile Picture Preview"
                  className="profile-edit-photo"
                />
              ) : profile.profilephotopath ? (
                <img
                  src={`${config.apiBaseUrl}/profilePhotos/${profile.profilephotopath}`}
                  alt="Profile Picture"
                  className="profile-edit-photo"
                />
              ) : (
                <div className="no-photo-message">No Profile Photo</div>
              )}
              {canEdit && (
                <div className="photo-edit-overlay">
                  <p>Click to Change Profile Photo</p>
                </div>
              )}
            </div>

            <div className="profile-edit-details">
              <p><strong>First Name:</strong>
                <input
                  type="text"
                  value={profile.firstname}
                  disabled={!canEdit}
                  onChange={(e) => setProfile({ ...profile, firstname: e.target.value })}
                />
              </p>
              <p><strong>Last Name:</strong>
                <input
                  type="text"
                  value={profile.lastname}
                  disabled={!canEdit}
                  onChange={(e) => setProfile({ ...profile, lastname: e.target.value })}
                />
              </p>
              <p><strong>Date of Birth:</strong>
                <input
                  type="text"
                  value={profile.birthdate}
                  disabled={!canEdit}
                  onChange={(e) => setProfile({ ...profile, birthdate: e.target.value })}
                />
              </p>
              <p><strong>Origin of Birth:</strong>
                <input
                  type="text"
                  value={profile.birthlocation}
                  disabled={!canEdit}
                  onChange={(e) => setProfile({ ...profile, birthlocation: e.target.value })}
                />
              </p>
              {profile.organization && (
                <p><strong>Organization:</strong>
                  <input
                    type="text"
                    value={profile.organization || ''}
                    disabled={!canEdit}
                    onChange={(e) => setProfile({ ...profile, organization: e.target.value })}
                  />
                </p>
              )}
              {/* Disable edit on these fields unless the user is admin */}
              <p><strong>Tournaments Played:</strong>
                <input
                  type="number"
                  value={profile.tournamentCount || 0}
                  disabled={!canEdit || getUserRole() !== 'ROLE_ADMIN'}
                  onChange={(e) => setProfile({ ...profile, tournamentCount: Number(e.target.value) })}
                />
              </p>
              <p><strong>Tournaments Won:</strong>
                <input
                  type="number"
                  value={profile.tournamentWinCount || 0}
                  disabled={!canEdit || getUserRole() !== 'ROLE_ADMIN'}
                  onChange={(e) => setProfile({ ...profile, tournamentWinCount: Number(e.target.value) })}
                />
              </p>
              <p><strong>Matches Played:</strong>
                <input
                  type="number"
                  value={profile.matchCount || 0}
                  disabled={!canEdit || getUserRole() !== 'ROLE_ADMIN'}
                  onChange={(e) => setProfile({ ...profile, matchCount: Number(e.target.value) })}
                />
              </p>
              <p><strong>Matches Won:</strong>
                <input
                  type="number"
                  value={profile.matchWinCount || 0}
                  disabled={!canEdit || getUserRole() !== 'ROLE_ADMIN'}
                  onChange={(e) => setProfile({ ...profile, matchWinCount: Number(e.target.value) })}
                />
              </p>
              <p><strong>Points:</strong>
                <input
                  type="number"
                  value={profile.points || 0}
                  disabled={!canEdit || getUserRole() !== 'ROLE_ADMIN'}
                  onChange={(e) => setProfile({ ...profile, points: Number(e.target.value) })}
                />
              </p>
            </div>
          </div>
          {canEdit && (
            <div className="button-edit-container">
              <button className="cancel-edit-button" onClick={handleCancel}>Cancel</button>
              <button className="save-edit-button" onClick={handleSave}>Save</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default EditProfile;