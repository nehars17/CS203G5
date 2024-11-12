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
        padding: '20px',
        backgroundColor: '#f8f9fa',
        borderRadius: '10px',
        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
    },
    title: {
        fontSize: '3rem',
        color: '#212529',
        marginBottom: '10px',
    },
    subTitle: {
        fontSize: '1.5rem',
        color: '#6c757d',
        marginBottom: '20px',
    },
    linksContainer: {
        display: 'flex' as const,
        flexWrap: 'wrap' as const,
        justifyContent: 'center' as const,
        gap: '15px',
    },
    link: {
        textDecoration: 'none',
        padding: '12px 24px',
        border: '2px solid #0d6efd',
        borderRadius: '8px',
        backgroundColor: '#0d6efd',
        color: '#ffffff',
        fontSize: '1.1rem',
        transition: 'background-color 0.3s, color 0.3s',
    },
    linkHover: {
        backgroundColor: '#0056b3',
        color: '#ffffff',
    },
};

export default Home;
