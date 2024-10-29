import React, { useState } from 'react';
import API from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container } from 'react-bootstrap';
import { GoogleLogin } from '@react-oauth/google'; // New Google OAuth import

const Register: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const userType = "ROLE_PLAYER";
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    try {
      await API.post('/register', {
        username: email,
        password: password,
        authorities: userType,
      });
      navigate('/login');
    } catch (error) {
      console.error('Registration failed', error);
      setError('Registration failed, please try again');
    }
  };

  const onSuccess = async (response: any) => {
    const userInfoResponse = await fetch(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${response.credential}`);
    const userInfo = await userInfoResponse.json();
    const email = userInfo.email;

    try {
      const res = await fetch('http://localhost:8080/googlelogin', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          tokenId: response.credential,
          email: email,
          role: userType
        }),
      });
      const data = await res.json();
      localStorage.setItem('token', data.token); // Store token
      console.log(data);
      navigate('/playerProfile');
     
    } catch (error) {
      console.error('Error during Google login:', error);
      setError('Google login failed, please try again');
    }
  };

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Register Player</h3>
          <Form onSubmit={handleSubmit}>
            <Form.Group controlId="formBasicEmail" className="mt-3">
              <Form.Label>Email address</Form.Label>
              <Form.Control
                type="email"
                placeholder="Enter email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </Form.Group>
            <Form.Group controlId="formBasicPassword" className="mt-3">
              <Form.Label>Password</Form.Label>
              <Form.Control
                type="password"
                placeholder="Password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </Form.Group>
            <Form.Group controlId="formConfirmPassword" className="mt-3">
              <Form.Label>Confirm Password</Form.Label>
              <Form.Control
                type="password"
                placeholder="Confirm password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
            </Form.Group>

            {error && <div className="alert alert-danger mt-3">{error}</div>} {/* Error message */}

            <Button variant="primary" type="submit" className="w-100 mt-4">
              Register
            </Button>
            <div className="mt-3">
              <GoogleLogin
                onSuccess={onSuccess}
                onError={() => console.error('Google login failed')} // Handle Google login failure
              />
            </div>
            <div className="text-center mt-3">
              <small>
                Already have an account? <a href="/login">Login</a>
              </small>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Register;
