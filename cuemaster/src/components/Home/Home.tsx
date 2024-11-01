import React, { useEffect, useState } from 'react';
import { Button, Container, Card } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import API from '../../services/api';

interface UserData {
  username: string;
}

const Home: React.FC = () => {
  const [userData, setUserData] = useState<UserData | null>(null);
  const navigate = useNavigate();

  const handleLogout = async () => {
    localStorage.removeItem('token');
    await fetch('http://localhost:8080/logout', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
    });
    navigate('/login');
  };

  const fetchUserProfile = async () => {
    try {
      const response = await API.get<UserData>('/me', {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token'),
        },
      });
      const data = response.data; // Assuming response.data is of type UserData
      setUserData(data); // Update state with user data
    } catch (error) {
      console.error('Failed to fetch profile', error);
      setUserData(null); // Optionally reset user data on error
    }
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '30rem', padding: '20px', textAlign: 'center' }}>
        <Card.Body>
          <h1>Welcome, {userData ? userData.username : 'User'}</h1>
          <Button onClick={handleLogout}>Logout</Button>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Home;
