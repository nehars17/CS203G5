import React, { useState, useEffect } from 'react';
import { Button, Modal, Form, Alert } from 'react-bootstrap';
import API from '../../services/api';
import { isAuthenticated, getUserIdFromToken } from '../authUtils';
import { useNavigate } from 'react-router-dom';
import './Account.css';

interface User {
    id: number;
    username: string;
}

const Account: React.FC = () => {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('***************');
    const [isGoogleUser, setIsGoogleUser] = useState<boolean>(false);
    const [showPasswordModal, setShowPasswordModal] = useState<boolean>(false);
    const [newPassword, setNewPassword] = useState<string>('');
    const [confirmPassword, setConfirmPassword] = useState<string>(''); // State for confirmation password
    const [error, setError] = useState<string>('');
    const [showAlert, setShowAlert] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(true);
    const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState<boolean>(false); // State for delete confirmation modal
    const navigate = useNavigate();
    const userId = getUserIdFromToken();
    const token = localStorage.getItem('token');

    useEffect(() => {
        const fetchUserData = async () => {
            if (isAuthenticated()) {
                try {
                    const userResponse = await API.get(`/user/${userId}`);
                    const userData = userResponse.data as User;
                    setUsername(userData.username);
                    const providerResponse = await API.get(`/Provider/${userId}`);
                    setIsGoogleUser(providerResponse.data === 'google');
                } catch (error) {
                    console.error('Error fetching user data:', error);
                } finally {
                    setLoading(false);
                }
            }
        };
    
        fetchUserData();
    }, [userId]);

    const handlePasswordChange = async () => {
        if (isGoogleUser) {
            setError('You cannot change the password because you registered with Google.');
            return;
        }
        if (newPassword !== confirmPassword) {
            setError('Passwords do not match. Please try again.');
            return;
        }
    
        try {
            // Create a User object to send along with the newPassword
            const user = {
                id: userId,
                username: username,  // Assuming you have 'username' in the state
                password: newPassword,
            };
    
            // Send the request with the User object
            await API.put(`/update/${userId}/password`, user, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });
            setPassword('***************'); // Mask the password after successful change
            setShowPasswordModal(false); // Close the modal
            setError(''); // Clear any previous error messages
        } catch (error) {
            setError('Error changing password. Please try again.');
        }
    };

    const handleDeleteAccount = async () => {
        try {
            await API.delete(`/user/${userId}/account`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            setShowAlert(true);
            localStorage.removeItem('token');
            navigate('/login');
        } catch (error) {
            setError('Error deleting account. Please try again.');
        }
    };

    const openDeleteConfirmModal = () => {
        setShowDeleteConfirmModal(true); // Show the delete confirmation modal
    };

    const closeDeleteConfirmModal = () => {
        setShowDeleteConfirmModal(false); // Close the delete confirmation modal
    };

    return (
        <div className="account-container">
            <h2 className="text-center mb-4">Account Settings</h2>
            <div className="mb-3">
                <strong>Username:</strong> {username}
            </div>
            <div className="mb-4">
                <strong>Password:</strong> {password}
            </div>
            {error && <Alert variant="danger" className="mb-3">{error}</Alert>}
            {showAlert && <Alert variant="success" className="mb-3">Your account has been deleted successfully.</Alert>}
            {!isGoogleUser && (
                <Button
                    variant="primary"
                    onClick={() => setShowPasswordModal(true)}
                    className="mb-3 w-100"
                >
                    Change Password
                </Button>
            )}
            {isGoogleUser && (
                <p className="text-muted">Password cannot be changed because you registered with Google.</p>
            )}
            <Button
                variant="danger"
                onClick={openDeleteConfirmModal} // Show the delete confirmation modal
                className="w-100"
            >
                Delete Account
            </Button>

            {/* Password Change Modal */}
            <Modal show={showPasswordModal} onHide={() => setShowPasswordModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Change Password</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group controlId="newPassword">
                            <Form.Label>New Password</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Enter new password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                            />
                        </Form.Group>
                        <Form.Group controlId="confirmPassword" className="mt-3">
                            <Form.Label>Confirm New Password</Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Re-enter new password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                            />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowPasswordModal(false)}>
                        Close
                    </Button>
                    <Button variant="primary" onClick={handlePasswordChange}>
                        Save Changes
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* Account Deletion Confirmation Modal */}
            <Modal show={showDeleteConfirmModal} onHide={closeDeleteConfirmModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Confirm Account Deletion</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Are you sure you want to delete your account? This action cannot be undone.</p>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={closeDeleteConfirmModal}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={handleDeleteAccount}>
                        Confirm Deletion
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
};

export default Account;