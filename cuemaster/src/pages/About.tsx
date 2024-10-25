import React from 'react';
import { Link } from 'react-router-dom';

const Home: React.FC = () => {
    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Cue Sports Management System</h1>
            <p style={styles.subTitle}>Manage your tournaments, profile, and stay updated with the leaderboard!</p>
            <div style={styles.linksContainer}>
                <Link to="/login" style={styles.link}>Login</Link>
                <Link to="/playerRegistration" style={styles.link}>Register Player</Link>
                <Link to="/organiserRegistration" style={styles.link}>Register Organiser</Link>
                <Link to="/tournaments" style={styles.link}>Tournaments</Link>
                <Link to="/leaderboard" style={styles.link}>Leaderboard</Link>
                {/* <Link to="/profile" style={styles.link}>Profile</Link> */}
            </div>
        </div>
    );
};

const styles = {
    container: {
        textAlign: 'center' as const,
        marginTop: '50px',
    },
    title: {
        fontSize: '2.5rem',
        color: '#333',
    },
    subTitle: {
        fontSize: '1.2rem',
        color: '#666',
    },
    linksContainer: {
        display: 'flex' as const,
        justifyContent: 'center' as const,
        marginTop: '20px',
        gap: '20px',
    },
    link: {
        textDecoration: 'none',
        padding: '10px 20px',
        border: '1px solid #333',
        borderRadius: '5px',
        backgroundColor: '#f0f0f0',
        color: '#333',
        fontSize: '1rem',
    },
};

export default Home;
