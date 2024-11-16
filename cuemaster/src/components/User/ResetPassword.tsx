import React, { useState, useEffect } from 'react';
import { Container, Card, Form, Button, Alert } from 'react-bootstrap';
import { useLocation,useNavigate } from 'react-router-dom';
import config from '../../config';

const ResetPassword: React.FC = () => {
    const [newPassword, setNewPassword] = useState<string>('');
    const [confirmNewPassword, setConfirmNewPassword] = useState<string>('');
    const [message, setMessage] = useState<string>('');
    const [error, setError] = useState<string>('');
    const navigate = useNavigate();

    // Use useLocation to access the query parameters
    const location = useLocation();
    const query = new URLSearchParams(location.search);
    const resetToken = query.get('token');
    const userId = query.get('id');

    useEffect(() => {
        if (!resetToken) {
            setError('Invalid reset link.');
        }
    }, [resetToken]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        if (newPassword !== confirmNewPassword) {
            setError("New passwords don't match.");
            return;
        }

        if (!resetToken) {
            setError('Missing or invalid token.');
            return;
        }

        try {
            const res = await fetch(`${config.apiBaseUrl}/resetPassword`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    password: newPassword,
                    token: resetToken,
                    user_id: userId
                }),
                
            });
            const errorText = await res.text();
            if(res.ok) {
            setMessage(errorText); // Assuming the message property is what you want to display
            setError(''); // Clear any previous errors
            navigate('/login');
            }
            else {
                setError(errorText || 'Failed to reset password. Please try again.');
            }
        } catch (error) {
            setMessage('');
            setError('Failed to reset password. Please try again.');
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
            <Card style={{ width: '25rem' }}>
                <Card.Body>
                    <h3 className="text-center mb-4">Reset Password</h3>
                    {message && <Alert variant="success">{message}</Alert>}
                    {error && <Alert variant="danger">{error}</Alert>}
                    <Form onSubmit={handleSubmit}>
                        <Form.Group controlId="formNewPassword">
                            <Form.Label>New Password</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Enter new password"
                                required
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                            />
                        </Form.Group>
                        <Form.Group controlId="formConfirmNewPassword" className="mt-3">
                            <Form.Label>Confirm New Password</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Confirm new password"
                                required
                                value={confirmNewPassword}
                                onChange={(e) => setConfirmNewPassword(e.target.value)}
                            />
                        </Form.Group>
                        <Button variant="primary" type="submit" className="w-100 mt-3">
                            Reset Password
                        </Button>
                    </Form>
                </Card.Body>
            </Card>
        </Container>
    );
};

export default ResetPassword;
