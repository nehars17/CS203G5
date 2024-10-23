import React, { useState } from 'react';
import API from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container } from 'react-bootstrap';
import { GoogleLogin } from 'react-google-login';

const Register: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [userType, setUserType] = useState<string>('ROLE_PLAYER');
  const clientId = '643146770456-qeusj14u53puh4bi1hhi2t8g21qhh1hc.apps.googleusercontent.com'; // Replace with your Google Client ID
  const navigate = useNavigate();

  const handleUserTypeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setUserType(event.target.value);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      console.error('Passwords do not match');
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
    }
  };

  const onSuccess = async (response: any) => {
    console.log('Login Success: currentUser:', response.profileObj);
    
    try {
      const res = await fetch('http://localhost:8080/googlelogin/player', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          tokenId: response.tokenId,
        }),
      });

      const data = await res.json();
      console.log(data);

      // Navigate or handle user data as needed
      if (data.role === 'ROLE_PLAYER') {
        navigate('/playerDashboard'); // Navigate to player dashboard after login
      }
    } catch (error) {
      console.error('Error during Google login:', error);
    }
  };

  const onFailure = (response: any) => {
    console.error('Login failed:', response);
  };

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Register</h3>
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

            <Form.Group controlId="formUserType" className="mt-3">
              <Form.Label>Select User Type</Form.Label>
              <div>
                <Form.Check 
                  type="radio" 
                  label="Player" 
                  value="ROLE_PLAYER" 
                  checked={userType === 'ROLE_PLAYER'} 
                  onChange={handleUserTypeChange} 
                  id="radioPlayer"
                  name="userType"
                />
                <Form.Check 
                  type="radio" 
                  label="Organiser" 
                  value="ROLE_ORGANISER" 
                  checked={userType === 'ROLE_ORGANISER'} 
                  onChange={handleUserTypeChange} 
                  id="radioOrganiser"
                  name="userType"
                />
              </div>
            </Form.Group>

            <Button variant="primary" type="submit" className="w-100 mt-4">
              Register
            </Button>

            <div className="mt-3">
              <GoogleLogin
                clientId={clientId}
                buttonText="Register with Google"
                onSuccess={onSuccess}
                onFailure={onFailure}
                cookiePolicy={'single_host_origin'}
                style={{ width: '100%' }}
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
