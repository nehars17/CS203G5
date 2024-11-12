import React, { useEffect, useState } from 'react';
import { Container, Card, Alert, Spinner } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import config from '../../config';

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
          const res = await fetch(`${config.apiBaseUrl}/activate`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token: activationToken }),
          });
  
          // Parse the response as text, since your backend returns a String
          const data = await res.text();
  
          if (!res.ok) {
            throw new Error(data || 'Activation failed');
          }
  
          // Success: set success message and stop loading
          setMessage(data);  // Display the success message returned by backend
          setError('');
          setLoading(false);
  
          // Navigate to login after a short delay (e.g., 3 seconds)
          setTimeout(() => {
            navigate('/login');
          }, 3000);
  
        } catch (error) {
          console.error(error);
         
          setError('Failed to Activate. Please try again.');
          
          setLoading(false);  // Stop loading if there's an error
        }
      } else {
        setError('Invalid activation token.');
        setLoading(false);
      }
    };
  
    activateAccount();
  }, [activationToken, navigate]);
  

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
            </>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default AccountActivated;
