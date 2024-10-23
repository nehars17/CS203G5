import React, { useEffect, useState } from 'react';
import API from '../../services/api';

const Profile: React.FC = () => {
    const [profile, setProfile] = useState<any>({});

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await API.get('/user/profile');
                setProfile(response.data);
            } catch (error) {
                console.error('Error fetching profile', error);
            }
        };
        fetchProfile();
    }, []);

    return (
        <div>
            <h2>Profile</h2>
            <p>Email: {profile.email}</p>
            <p>Joined: {profile.joinedDate}</p>
        </div>
    );
};

export default Profile;
