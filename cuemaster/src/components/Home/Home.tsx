import React, { useEffect, useState } from 'react';
import { Button, Container, Card, Row, Col } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import API from '../../services/api';
import config from '../../config';

interface UserData {
  username: string;
}

const Home: React.FC = () => {
  const [userData, setUserData] = useState<UserData | null>(null);
  const navigate = useNavigate();

  const handleLogout = async () => {
    localStorage.removeItem('token');
    await fetch(`${config.apiBaseUrl}/logout`, {
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
      const data = response.data;
      setUserData(data);
    } catch (error) {
      console.error('Failed to fetch profile', error);
      setUserData(null);
    }
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '24rem', padding: '2rem', boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)', borderRadius: '8px' }}>
        <Card.Body className="text-center">
          <h2 style={{ fontWeight: 500, color: '#333', marginBottom: '1.5rem' }}>
            Cue Sport 9-Ball Manager
          </h2>
          <p style={{ color: '#666', fontSize: '1rem' }}>
            Welcome, {userData ? userData.username : 'Player'}
          </p>

          <Row className="my-4">
            <Col>
              <Button
                variant="primary"
                size="lg"
                block
                onClick={() => navigate('/tournaments')}
                style={{
                  backgroundColor: '#3f51b5',
                  borderColor: '#3f51b5',
                  padding: '0.75rem 1rem',
                  fontSize: '1rem',
                  borderRadius: '8px',
                }}
              >
                View Tournaments
              </Button>
            </Col>
          </Row>

          <Row>
            <Col>
              <Button
                variant="outline-secondary"
                size="lg"
                block
                onClick={handleLogout}
                style={{
                  padding: '0.75rem 1rem',
                  fontSize: '1rem',
                  borderRadius: '8px',
                }}
              >
                Logout
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Home;
