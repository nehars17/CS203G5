import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Card } from 'react-bootstrap';
import config from '../../config';
import { setAuthToken } from '../../services/api';
import { getUserIdFromToken } from '../../components/authUtils';

const EmailAuthForm: React.FC = () => {
  const [error, setError] = useState('');
  const [code, setCode] = useState('');
  const location = useLocation();
  const navigate = useNavigate();

  // Extract user and role data from location state
  const user = location.state?.data?.user;
  const role = location.state?.data?.role;

  const handleVerifyCode = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const res = await fetch(`${config.apiBaseUrl}/verify-code`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          user: user,
          code: code,
          role: role,
        }),
      });

      if (!res.ok) {
        throw new Error('Verification failed');
      }

      const data = await res.json();
      localStorage.setItem('token', data.token);
      setAuthToken(data.token);

      const userId = getUserIdFromToken();

      // Check if the profile exists
      const profileRes = await fetch(`${config.apiBaseUrl}/profile/${userId}`);
      const profile = profileRes.ok ? await profileRes.json() : null;

      // Navigate based on role and profile status
      if (role === 'ROLE_PLAYER' || role === 'ROLE_ORGANISER') {
        if (!profile) {
          window.location.href = '/ProfileCreation';
        } else {
          window.location.href = '/home';
        }
      } else {
        window.location.href = '/adminDashboard';
      }

    } catch (error) {
      console.error('Authentication failed:', error);
      setError('Authentication failed, please retry.');
    }
  };

  if (!user) {
    return <p>Please Log In</p>;
  }

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Row>
        <Col md={{ span: 6, offset: 3 }}>
          <Card>
            <Card.Body>
              <h3 className="text-center mb-4">Email Authentication for {user.username}</h3>
              {error && <Alert variant="danger">{error}</Alert>}
              <Form onSubmit={handleVerifyCode}>
                <Form.Group controlId="formAuthCode">
                  <Form.Label>Authentication Code:</Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="Enter authentication code"
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    required
                  />
                </Form.Group>
                <Button variant="primary" type="submit" className="w-100 mt-4">
                  Verify Code
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default EmailAuthForm;
