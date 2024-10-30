import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container, Alert } from 'react-bootstrap';
import { GoogleLogin } from '@react-oauth/google';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(''); // Add error state
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch('http://localhost:8080/normallogin', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: email,
          password: password,
        }),
      });

      if (!res.ok) {
        throw new Error('Login failed');
      }

      const data = await res.json();
      localStorage.setItem('token', data.token); // Store token

      // Navigate based on role
      if (data.role === 'ROLE_PLAYER') {
        
        navigate('/playerProfile');
      } else if (data.role === 'ROLE_ORGANISER') {
        navigate('/organiserProfile');
      } else {
        navigate('/adminDashboard');
      }
    } catch (error) {
      console.error('Login failed:', error);
      setError('Login failed, please check your credentials and try again.'); // Set error message
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

        }),
      });
      const data = await res.json();
      console.log(data.role);
      console.log(data);
      localStorage.setItem('token', data.token); // Store token
      if (data.role == "ROLE_PLAYER") {
        navigate('/playerProfile');
      }
      else if(data.role == "ROLE_ORGANISER") {
        navigate('/organiserProfile');
      }

    } catch (error) {
      console.error('Error during Google login:', error);
      setError('Google login failed, please try again.');
    }
  };



  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Login</h3>
          {error && <Alert variant="danger">{error}</Alert>} {/* Show error alert */}
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
              onSuccess={onSuccess}
              onError={() => console.error('Google login failed')} // Handle Google login failure

            />
          </div>
          <div className="text-center mt-3">
            <small>
              Don't have an account? 
              <br></br>
              <a href="/playerRegistration">Player Registration</a>
              <br></br>
              <a href="/organiserRegistration">Organiser Registration</a>
            </small>
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Login;
