import React, { useState, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import config from '../../config';

interface User {
    id: number;
    username: string;
    status: 'active' | 'locked';
    enabled: boolean;
    unlocked: boolean;
    provider: string;
}

const AdminDashboard: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]); // Initial state is an empty array
    const [error, setError] = useState<string>(''); // Error state
    const [loading, setLoading] = useState<boolean>(true); // Loading state

    // Fetch users from the backend when the component mounts
    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const res = await fetch(`${config.apiBaseUrl}/users`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (!res.ok) {
                    const errorText = await res.text(); // Extract backend error message
                    throw new Error(errorText);
                }

                const usersData = await res.json();
                setUsers(usersData); // Update the users state with the fetched data
                setError(''); // Clear any previous errors
            } catch (error) {
                setUsers([]); // Clear the users if an error occurs
                setError((error instanceof Error) ? error.message : 'Unexpected Error'); // Check if it's an instance of Error
            } finally {
                setLoading(false); // Set loading to false after the request is complete
            }
        };

        fetchUsers(); // Call the function to fetch users
    }, []); // Empty dependency array ensures this runs only once when the component mounts

    // Delete a user from the list
    const deleteUser = async (id: number) => {
        // Set the token when the app starts or user logs in
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`${config.apiBaseUrl}/user/${id}/account`, { // Correct URL interpolation
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                const errorText = await res.text(); // Extract backend error message
                throw new Error(errorText);
            }

            setUsers(users.filter(user => user.id !== id)); // Update the users state
        } catch (error) {
            setError((error instanceof Error) ? error.message : 'Unexpected Error');
        }
    };

    // Unlock a user (change status to 'active')
    const unlockUser = async (id: number) => {
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`${config.apiBaseUrl}/user/${id}/account`, { // Correct URL interpolation
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                const errorText = await res.text(); // Extract backend error message
                throw new Error(errorText);
            }

            setUsers(users.map(user =>
                user.id === id ? { ...user, unlocked: true } : user
            ));
        } catch (error) {
            setError((error instanceof Error) ? error.message : 'Unexpected Error');
        }
    };

    // View profile action 
    const viewProfile = async (id: number) => {
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`${config.apiBaseUrl}/user/${id}/profile/${id}`, { 
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                const errorText = await res.text(); // Extract backend error message
                throw new Error(errorText);
            }

            // Implement routing to a detailed profile page here if needed
        } catch (error) {
            setError((error instanceof Error) ? error.message : 'Unexpected Error');
        }
    };

    return (
        <div className="container mt-4">
            <h1>User Management</h1>

            {/* Display loading message */}
            {loading && <div>Loading users...</div>}

            {/* Display error message */}
            {error && <div className="alert alert-danger">{error}</div>}

            {/* Table for listing users */}
            <table className="table table-bordered mt-3">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>Enabled</th>
                        <th>Status</th>
                        <th>Provider</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.username}</td>
                            <td>{user.enabled ? 'Yes' : 'No'}</td>
                            <td>{user.unlocked ? 'active' : 'locked'}</td>
                            <td>{user.provider}</td>
                            <td>
                                <button className="btn btn-info btn-sm me-2" onClick={() => viewProfile(user.id)}>View Profile</button>
                                {user.unlocked === false && (
                                    <button className="btn btn-warning btn-sm me-2" onClick={() => unlockUser(user.id)}>Unlock</button>
                                )}
                                <button className="btn btn-danger btn-sm" onClick={() => deleteUser(user.id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminDashboard;
