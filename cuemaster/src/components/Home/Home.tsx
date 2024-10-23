import React from 'react';
import { Button, Container, Card } from 'react-bootstrap';

const Home: React.FC = () => {
  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '30rem', padding: '20px', textAlign: 'center' }}>
        <Card.Body>
          <h1>Welcome, User!</h1>
          <p>You have successfully logged in.</p>
          {/* <Button variant="primary" onClick={onLogout}>
            Logout
          </Button> */}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Home;
