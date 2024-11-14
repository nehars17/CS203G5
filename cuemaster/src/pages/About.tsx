import React from 'react';
import { Link } from 'react-router-dom';
import './About.css'; // Adding a CSS file for better style management

const Home: React.FC = () => {
    return (
        <div className="home-container">
            <div className="home-content">
                <h1 className="home-title">Cue Master</h1>
                <p className="home-subTitle">Manage your tournaments, profile, and stay updated with the leaderboard!</p>
                
                <div className="home-links-container">
                    <Link to="/login" className="home-link">
                        <img src="https://cdn-icons-png.flaticon.com/512/1828/1828506.png" alt="Login" className="link-icon" />
                        Login
                    </Link>
                    <Link to="/playerRegistration" className="home-link">
                        <img src="https://cdn-icons-png.flaticon.com/512/2922/2922510.png" alt="Register Player" className="link-icon" />
                        Register Player
                    </Link>
                    <Link to="/organiserRegistration" className="home-link">
                        <img src="https://cdn-icons-png.flaticon.com/512/2922/2922510.png" alt="Register Organiser" className="link-icon" />
                        Register Organiser
                    </Link>
                    <Link to="/tournaments" className="home-link">
                        <img src="https://cdn-icons-png.flaticon.com/512/2936/2936837.png" alt="Tournaments" className="link-icon" />
                        Tournaments
                    </Link>
                    <Link to="/leaderboard" className="home-link">
                        <img src="https://cdn-icons-png.flaticon.com/512/1828/1828884.png" alt="Leaderboard" className="link-icon" />
                        Leaderboard
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default Home;
