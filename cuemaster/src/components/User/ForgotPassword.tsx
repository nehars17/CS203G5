import React, { useState } from 'react';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import axios from 'axios';

const ForgotPassword: React.FC = () => {
    const [email, setEmail] = useState<string>('');
    const [message, setMessage] = useState<string>('');
    const [error, setError] = useState<string>('');

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        try {
            const res = await fetch('http://localhost:8080/forgotPassword', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username: email }),
            });
    
            if (!res.ok) {
                const errorText = await res.text(); // Extract backend error message
                throw new Error(errorText);
            }
    
            setMessage('Reset link sent to your email.');
            setError(''); // Clear any previous errors
    
        } catch (error: any) {
            setMessage('');
            setError(error.message || 'Unexpected Error'); // Display backend error message
        }
    };
    
    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
            <Card style={{ width: '25rem' }}>
                <Card.Body>
                    <h3 className="text-center mb-4">Forgot Password</h3>
                    {message && <Alert variant="success">{message}</Alert>}
                    {error && <Alert variant="danger">{error}</Alert>}
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
                        <Button variant="primary" type="submit" className="w-100 mt-3">
                            Send Reset Link
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ForgotPassword;
