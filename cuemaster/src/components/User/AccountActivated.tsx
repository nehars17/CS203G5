import React, { useEffect, useState } from 'react';
import { Container, Card, Alert, Spinner } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const AccountActivated: React.FC = () => {
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const activationToken = query.get('token');

  useEffect(() => {
    const activateAccount = async () => {
      if (activationToken) {
        try {
          const res = await fetch('http://localhost:8080/activate', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token:activationToken }),
          });
          const data = await res.json();
          console.log(data);
          setMessage(data);
          setError('');
          navigate("/login")
        } catch (error) {
          console.log(error)
          setMessage('');
          setError('Failed to Activate. Please try again.');
          setLoading(false);
        }
      }
    };
    activateAccount();
  }, [activationToken]);

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Account Activation</h3>
          {loading ? (
            <div className="text-center">
              <Spinner animation="border" />
            </div>
          ) : (
            <>
              {message && <Alert variant="success">{message}</Alert>}
              {error && <Alert variant="danger">{error}</Alert>}
              {message && navigate('/login')}
            </>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default AccountActivated;