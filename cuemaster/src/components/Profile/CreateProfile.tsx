import React, { useState,useEffect } from 'react';
import API from '../../services/api';
import { useNavigate, useLocation } from 'react-router-dom';
import { format } from 'date-fns';
import { Form, Button, Card, Container } from 'react-bootstrap';

const CreateProfile: React.FC = () => {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [birthDate, setBirthDate] = useState('');
    const [birthLocation, setBirthLocation] = useState('');
    const navigate = useNavigate();
    const location = useLocation();

    // useEffect(() => {
    //   const token = localStorage.getItem('token');
    //   if (!token) {
    //       alert("You must be logged in to create a profile.");
    //       navigate('/login'); // Redirect to login page if not logged in
    //   }
    // }, [navigate]); // Dependency array ensures this runs when the component mounts

    const formatDate = (dateString: string) => {
      try {
        const parsedDate = new Date(dateString);
        return format(parsedDate, 'yyyy-MM-dd');                                            // Format to yyyy-MM-dd
      } catch (error) {
        console.warn('Invalid date format:', error);
        return '';
      }
    };

    const handleSubmit = async (event: React.FormEvent) => {                                // async allows the use of await (pauses the execution of the function until promis is resolved or rejected)
        event.preventDefault();                                                             // Prevent the default form submission

        if (!firstName || !lastName || !birthDate || !birthLocation) {                      // Validate all variables are filled 
            alert("All fields are required!");
            return;
        }

        const formattedBirthDate = formatDate(birthDate);
        const userId = location.state?.userId;

        const profileData = {
            firstName,
            lastName,
            birthDate: formattedBirthDate,
            birthLocation,
        };

        try{     
            const response = await API.post(`/user/${userId}/profile`, profileData);

            console.log("Profile created successfully:", response.data);  
            navigate('/');                                                                  // After successful submission, navigate to home page                  

        }catch (error){
            console.error('Profile Creation failed', error);
        }

    };
    return (
        <div>
          <h2>Create Profile</h2>
          <form onSubmit={handleSubmit}>
            <div>
              <label>
                First Name:
                <input
                  type="text"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </label>
            </div>
            <div>
              <label>
                Last Name:
                <input
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </label>
            </div>
            <div>
              <label>
                Birth Date:
                <input
                  type="date"
                  value={birthDate}
                  onChange={(e) => setBirthDate(e.target.value)}
                  required
                />
              </label>
            </div>
            <div>
              <label>
                Birth Location:
                <input
                  type="text"
                  value={birthLocation}
                  onChange={(e) => setBirthLocation(e.target.value)}
                  required
                />
              </label>
            </div>
            <button type="submit">Create Profile</button>
          </form>
        </div>
    );
};
    
export default CreateProfile;