import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Card } from 'react-bootstrap';

const EmailAuthForm: React.FC = () => {
  const [error, setError] = useState('');
  const [code, setCode] = useState('');
  const location = useLocation();
  const user = location.state?.data.user;
  const role = location.state?.data.role;

  const navigate = useNavigate();

  const handleVerifyCode = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await fetch('http://localhost:8080/verify-code', {
        method: 'POST', // Ensure it's POST
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          user: user,
          code: code,
          role: role
        })
      });

      if (!res.ok) {
        throw new Error('Verification failed');
      }

      const data = await res.json();
      localStorage.setItem('token', data.token); // Store token
      
      // Navigate based on role
      if (role == 'ROLE_PLAYER') {
        navigate('/playerProfile');
      } else if (role == 'ROLE_ORGANISER') {
        navigate('/organiserProfile');
      } else {
        navigate('/adminDashboard');
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
          <Card.Body> <h3 className="text-center mb-6">Email Authentication for {user.username}</h3>
            {error && <Alert variant="danger">{error}</Alert>} 
            <Form onSubmit={handleVerifyCode}> <Form.Group controlId="formAuthCode"> 
              <Form.Label>Authentication Code:</Form.Label> <Form.Control type="text" placeholder="Enter authentication code" value={code} onChange={(e) => setCode(e.target.value)} required /> 
                </Form.Group> <Button variant="primary" type="submit" className="w-100 mt-4"> Verify Code </Button>
                 </Form> 
                </Card.Body> </Card> </Col> </Row> </Container>); 

};
export default EmailAuthForm;
