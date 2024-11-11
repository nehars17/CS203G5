
import React from 'react';
import { Container, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

const Forbidden403: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <div className="text-center">
        <h1>403 - Forbidden</h1>
        <p>You don't have permission to access this page.</p>
        <Button onClick={() => navigate('/')} variant="primary">Go Back Home</Button>
      </div>
    </Container>
  );
};

export default Forbidden403;
