import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Card } from 'react-bootstrap';
import config from '../../config';
import { setAuthToken } from '../../services/api';
import { isAuthenticated, getUserIdFromToken, getUserRole } from '../../components/authUtils';
 
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
      const res = await fetch(`${config.apiBaseUrl}/verify-code`, {
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
      setAuthToken(localStorage.getItem('token'));      // Navigate based on role
      console.log(role);
      const user_id= getUserIdFromToken();
      const profile = null;

      try {
        const res = await fetch(`${config.apiBaseUrl}/profile/${user_id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!res.ok) {
            const profile = await res.json(); // Extract backend error message
            
        }

     
      } catch (error) {
        setError((error instanceof Error) ? error.message : 'Unexpected Error'); 
      }
      if (role === 'ROLE_PLAYER') {
        window.location.href = '/playerProfile';
        if(profile==null){
        window.location.href = '/ProfileCreation';
        }


      } else if (role === 'ROLE_ORGANISER') {
        if(profile==null){
          window.location.href = '/ProfileCreation';

          }
          window.location.href = '/organiserProfile';
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
          <Card.Body> <h3 className="text-center mb-6">Email Authentication for {user.username}</h3>
            {error && <Alert variant="danger">{error}</Alert>} 
            <Form onSubmit={handleVerifyCode}> <Form.Group controlId="formAuthCode"> 
              <Form.Label>Authentication Code:</Form.Label> <Form.Control type="text" placeholder="Enter authentication code" value={code} onChange={(e) => setCode(e.target.value)} required /> 
                </Form.Group> <Button variant="primary" type="submit" className="w-100 mt-4"> Verify Code </Button>
                 </Form> 
                </Card.Body> </Card> </Col> </Row> </Container>); 

};
export default EmailAuthForm;
