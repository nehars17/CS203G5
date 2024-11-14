import React, { useState, useEffect } from 'react';
import API from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { isAuthenticated,getUserIdFromToken, getUserRole } from 'cuemaster/src/components/authUtils';
import config from '../../config';

const CreateProfile: React.FC = () => {
  //Variables - uses useState to add state to functional components 
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [birthLocation, setBirthLocation] = useState('');
  const [profilePhoto, setProfilePhoto] = useState<File | null>(null);
  const [organisation, setOrganisation] = useState('');
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const navigate = useNavigate();
  const userRole = getUserRole();
  const userId = getUserIdFromToken();
  const token = localStorage.getItem('token');

  //Function that format the Date to the right format for LocalDate Object 
  const formatDate = (dateString: string): string => {
    try {
      // Split the input string into day, month, and year
      const [day, month, year] = dateString.split('-');
  
      // Create a new date object in YYYY-MM-DD format
      const parsedDate = new Date(`${year}-${month}-${day}`);
  
      // Check if the date is valid
      if (isNaN(parsedDate.getTime())) {
        throw new Error('Invalid date');
      }
  
      // Get the year, month, and day
      const formattedYear = parsedDate.getFullYear();
      const formattedMonth = String(parsedDate.getMonth() + 1).padStart(2, '0'); // Months are zero-based
      const formattedDay = String(parsedDate.getDate()).padStart(2, '0');
  
      // Format as 'yyyy-MM-dd'
      return `${formattedYear}-${formattedMonth}-${formattedDay}`;
    } catch (error) {
      console.warn('Invalid date format:', error);
      return ''; // Return an empty string if there's an error
    }
  };

  //Function that handle the input of a profilephoto file and generate a preview for it 
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setProfilePhoto(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };

  //Function that handle the form submission
  const handleSubmit = async (event: React.FormEvent) => {

    if (!isAuthenticated()) {
      alert("You must be logged in to create a profile.");
      return;
    }

    console.log(userId);

    // Prevent the default form submission
    event.preventDefault();

    // Validate all variables are filled 
    if (!firstName || !lastName || !birthDate || !birthLocation || !profilePhoto) {
      alert("All fields are required!");
      return;
    }

    //Formatting the date by calling formatDate Function 
    const formattedBirthDate = formatDate(birthDate);

    //Creating an profileData object that stores the Profile values
    const profileData = {
      firstname: firstName,
      lastname: lastName,
      birthdate: formattedBirthDate,
      birthlocation: birthLocation,
      organization: userRole === 'ROLE_ORGANISER' ? organisation : undefined, 
    };

    // Create a FormData object
    const formData = new FormData();

    // Append the profile data as a Blob
    formData.append("profile", new Blob([JSON.stringify(profileData)], { type: "application/json" }));
    // Append the profile photo file
    formData.append("profilePhoto", profilePhoto);

    try {
      const response = await fetch(`${config.apiBaseUrl}/create/profile/${userId}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`, // Include the token in the Authorization header
        },
        body: formData,
      });

      if (!response.ok) {
        const errorResponse = await response.json();
        const errorMessage = errorResponse.message || 'An unexpected error occurred.';
        alert(`Profile creation failed: ${errorMessage}`);
        return;
      }

      navigate(`/`);
    } catch (error) {
      console.error('An unexpected error occurred:', error);
      alert('An unexpected error occurred. Please try again later.');
    }
  };

  return (
    <div style={{ maxWidth: "400px", margin: "50px auto", padding: "20px", backgroundColor: "#f9f9f9", borderRadius: "8px", boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)" }}>
      <h2 style={{ textAlign: "center", marginBottom: "20px", color: "#333" }}>Create Profile</h2>
      <form onSubmit={handleSubmit}>
        {/* Profile Photo Field */}
        <div>
          <label style={{ display: "block", marginBottom: "10px", fontWeight: "bold", color: "#555" }}>
            Profile Photo:
            <input
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              required
              style={{ width: "100%", padding: "10px", margin: "10px 0 20px 0", borderRadius: "4px", border: "1px solid #ddd" }}
            />
          </label>
        </div>
        {previewUrl && (
          <div style={{ textAlign: 'left', marginBottom: '10px' }}>
            <h4>Profile Photo Preview:</h4>
            <img
              src={previewUrl}
              alt="Profile preview"
              style={{
                width: '150px',
                height: '150px',
                objectFit: 'cover',
                borderRadius: '8px',
                marginTop: '10px'
              }}
            />
          </div>
        )}
        {/* First Name Field */}
        <div>
          <label style={{ display: "block", marginBottom: "10px", fontWeight: "bold", color: "#555" }}>
            First Name:
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
              style={{ width: "100%", padding: "10px", margin: "10px 0 20px 0", borderRadius: "4px", border: "1px solid #ddd" }}
            />
          </label>
        </div>
        {/* Last Name Field */}
        <div>
          <label style={{ display: "block", marginBottom: "10px", fontWeight: "bold", color: "#555" }}>
            Last Name:
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
              style={{ width: "100%", padding: "10px", margin: "10px 0 20px 0", borderRadius: "4px", border: "1px solid #ddd" }}
            />
          </label>
        </div>
        {/* Birth Date Field */}
        <div>
          <label style={{ display: "block", marginBottom: "10px", fontWeight: "bold", color: "#555" }}>
            Date of Birth:
            <input
              type="date"
              value={birthDate}
              onChange={(e) => setBirthDate(e.target.value)}
              required
              style={{ width: "100%", padding: "10px", margin: "10px 0 20px 0", borderRadius: "4px", border: "1px solid #ddd" }}
            />
          </label>
        </div>
        {/* Birth Location Field */}
        <div>
          <label style={{ display: "block", marginBottom: "10px", fontWeight: "bold", color: "#555" }}>
            Origin of Birth:
            <input
              type="text"
              value={birthLocation}
              onChange={(e) => setBirthLocation(e.target.value)}
              required
              style={{ width: "100%", padding: "10px", margin: "10px 0 20px 0", borderRadius: "4px", border: "1px solid #ddd" }}
            />
          </label>
        </div>
        {/* Organization Name Field (conditional rendering) */}
        {userRole === 'ROLE_ORGANISER' && (
          <div>
            <label style={{ display: "block", marginBottom: "10px", fontWeight: "bold", color: "#555" }}>
              Organisation Name:
              <input
                type="text"
                value={organisation}
                onChange={(e) => setOrganisation(e.target.value)}
                required
                style={{ width: "100%", padding: "10px", margin: "10px 0 20px 0", borderRadius: "4px", border: "1px solid #ddd" }}
              />
            </label>
          </div>
        )}
        <button type="submit" style={{ width: "100%", padding: "12px", backgroundColor: "#4CAF50", color: "white", border: "none", borderRadius: "4px", cursor: "pointer", fontWeight: "bold" }}>
          Create Profile
        </button>
      </form>
    </div>
  );
};

export default CreateProfile;