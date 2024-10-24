import React, { useState } from 'react';
import API, { setAuthToken } from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container } from 'react-bootstrap';


const Login: React.FC = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            // const response = await API.post('/normallogin', { email, password });
            const response = await API.post('/normallogin', {
              username: email,
              password: password,
              authorities: "ROLE_PLAYER"
          });
            const { token } = response.data;
            setAuthToken(token); // Set token in axios
            localStorage.setItem('token', token); // Store token in localStorage
            navigate('/profile');
        } catch (error) {
            console.error('Login failed', error);
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Login</h3>
          <Form>
            <Form.Group controlId="formBasicEmail">
              <Form.Label>Email address</Form.Label>
              <Form.Control type="email" placeholder="Enter email" required />
            </Form.Group>

            <Form.Group controlId="formBasicPassword" className="mt-3">
              <Form.Label>Password</Form.Label>
              <Form.Control type="password" placeholder="Password" required />
            </Form.Group>

            <Button variant="primary" type="submit" className="w-100 mt-4">
              Login
            </Button>

            <div className="text-center mt-3">
              <small>
                Don't have an account? <a href="/register">Register</a>
              </small>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Login;
