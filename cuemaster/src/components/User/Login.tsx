import React, { useState } from 'react';
import API, { setAuthToken } from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container } from 'react-bootstrap';
import { GoogleLogin } from '@react-oauth/google';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await API.post('/normallogin', {
        username: email,
        password: password,
      });
      const { token, id:userId} = response.data;
      console.log('Response from API:', response);
      setAuthToken(token); // Set token in axios
      localStorage.setItem('token', token); // Store token in localStorage
      localStorage.setItem('userId', userId);
      navigate(`/create-profile/${userId}`);
    } catch (error) {
      console.error('Login failed', error);
    }
  };

  const handleGoogleLoginSuccess = async (response: any) => {
    console.log('Login Success:', response);
    console.log('Login Success:', response.data);
    const userInfoResponse = await fetch(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${response.credential}`);
    const userInfo = await userInfoResponse.json();
    const email = userInfo.email
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
         }),
      });
      const data = await res.json();
      console.log(data);
      if (data.role === 'ROLE_PLAYER') {
        navigate('/playerDashboard');
      }
      else if (data.role === 'ROLE_ORGANISER') {
        navigate('/organiserDashboard');
      }
     
    } catch (error) {
      console.error('Error during Google login:', error);
    }
  };

  const handleGoogleLoginError = () => {
    console.error('Google Login Failed');
  };

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Login</h3>
          <Form onSubmit={handleSubmit}>
            <Form.Group controlId="formBasicEmail">
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
            <Button variant="primary" type="submit" className="w-100 mt-4">
              Login
            </Button>
          </Form>
          <div className="mt-3">
            <GoogleLogin
              onSuccess={handleGoogleLoginSuccess}
              onError={handleGoogleLoginError}
            />
          </div>
          <div className="text-center mt-3">
            <small>
              Don't have an account? <a href="/register">Register</a>
            </small>
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Login;
