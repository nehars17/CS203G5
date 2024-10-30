import React, { useState, useEffect } from 'react';
import API from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { AxiosError } from 'axios';

const CreatePlayerProfile: React.FC = () => {
  //Variables - uses useState to add state to functional components 
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthDate, setBirthDate] = useState('');
  const [birthLocation, setBirthLocation] = useState('');
  const [profilePhoto, setProfilePhoto] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const navigate = useNavigate();
  const userId = localStorage.getItem('userId');

  //Function that handle Axios Error 
  const isAxiosError = (error: unknown): error is AxiosError => {
    return (error as AxiosError).isAxiosError !== undefined;
  };

  //Function that format the Date to the right format for LocalDate Object 
  const formatDate = (dateString: string) => {
    try {
      const parsedDate = new Date(dateString);
      return format(parsedDate, 'yyyy-MM-dd');
    } catch (error) {
      console.warn('Invalid date format:', error);
      return '';
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
    };

    // Create a FormData object
    const formData = new FormData();

    // Append the profile data as a Blob
    formData.append("profile", new Blob([JSON.stringify(profileData)], { type: "application/json" }));

    // Append the profile photo file
    formData.append("profilePhoto", profilePhoto);

    try {
      //Calling the post method and passing the profile FormData object 
      const response = await API.post(`/user/${userId}/profile`, formData);

      //Navigate to the home page upon successful creation of profile 
      navigate(`/`);

    } catch (error: unknown) {
      const isAxiosErr = isAxiosError(error);
      const axiosError = isAxiosErr ? (error as AxiosError) : null;

      // Safely extract the error message with type assertion
      const errorMessage = (axiosError?.response?.data as { message?: string })?.message || axiosError?.message || 'An unexpected error occurred.';

      if (isAxiosErr) {
        if (errorMessage === "Profile already exists.") {
          alert("A profile for this user already exists. Please update it instead.");
        } else if (errorMessage?.includes("profilePhoto")) {
          alert(`Profile photo upload failed: ${errorMessage}`);
        } else {
          alert(`Profile creation failed: ${errorMessage}`);
        }
      } else {
        console.error('An unexpected error occurred:', error);
        alert('An unexpected error occurred. Please try again later.');
      }
    }
  };
  return (
    <div style={{ maxWidth: "400px", margin: "50px auto", padding: "20px", backgroundColor: "#f9f9f9", borderRadius: "8px", boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)" }}>
      <h2 style={{ textAlign: "center", marginBottom: "20px", color: "#333" }}>Create Profile</h2>
      <form onSubmit={handleSubmit}>
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
                width: '150px',     // Set desired fixed width
                height: '150px',    // Set desired fixed height
                objectFit: 'cover', // Ensures image fills the area without distortion
                borderRadius: '8px', // Optional: Rounded corners
                marginTop: '10px'
              }}
            />
          </div>
        )}
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
        <button type="submit" style={{ width: "100%", padding: "12px", backgroundColor: "#4CAF50", color: "white", border: "none", borderRadius: "4px", cursor: "pointer", fontWeight: "bold" }}>
          Create Profile
        </button>
      </form>
    </div>
  );
};

export default CreatePlayerProfile;