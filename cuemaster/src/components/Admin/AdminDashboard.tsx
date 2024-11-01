import React, { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

interface User {
    id: number;
    name: string;
    email: string;
    status: 'active' | 'locked';
}

const initialUsers: User[] = [
    { id: 1, name: 'Alice Johnson', email: 'alice@example.com', status: 'active' },
    { id: 2, name: 'Bob Smith', email: 'bob@example.com', status: 'locked' },
    { id: 3, name: 'Charlie Brown', email: 'charlie@example.com', status: 'active' },
];

const AdminDashboard: React.FC = () => {
    const [users, setUsers] = useState<User[]>(initialUsers);

    const deleteUser = (id: number) => {
        setUsers(users.filter(user => user.id !== id));
    };

    const unlockUser = (id: number) => {
        setUsers(users.map(user => 
            user.id === id ? { ...user, status: 'active' } : user
        ));
    };

    const viewProfile = (id: number) => {
        alert(`Viewing profile for user ID: ${id}`);
        // You can implement routing to a detailed profile page here
    };

    return (
        <div className="container mt-4">
            <h1>User Management</h1>
            <table className="table table-bordered mt-3">
                <thead className="table-light">
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.id}</td>
                            <td>{user.name}</td>
                            <td>{user.email}</td>
                            <td>{user.status}</td>
                            <td>
                                <button className="btn btn-info btn-sm me-2" onClick={() => viewProfile(user.id)}>View Profile</button>
                                {user.status === 'locked' ? (
                                    <button className="btn btn-warning btn-sm me-2" onClick={() => unlockUser(user.id)}>Unlock</button>
                                ) : null}
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
