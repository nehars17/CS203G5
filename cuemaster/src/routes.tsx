import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Login from './components/User/Login';
import Register from './components/User/Register';
import CreateProfile from './components/Profile/CreateProfile';
import Profile from './components/Profile/Profile';
import Matches from './components/Matches/Matches';
import Leaderboard from './components/Leaderboard/Leaderboard';
import Tournament from './components/Tournament/Tournament';
import NavBar from './components/NavBar/NavBar';
import Error404 from './pages/Error404';

import Home from './components/Home/Home';
import About from './pages/About';



const AppRoutes: React.FC = () => {
    return (
        <Router>
            <NavBar />

            <Routes>
                <Route path="/" element={<About />} />
                <Route path="/home" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/playerRegistration" element={<RegisterPlayer />} />
                <Route path="/organiserRegistration" element={<RegisterOrganiser />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/matches" element={<Matches />} />
                <Route path="/leaderboard" element={<Leaderboard />} />
                <Route path="/tournaments" element={<Tournament />} />  {/* Add the route here */}
                <Route path="/create-profile/:userId" element={<CreateProfile />} />
                <Route path="*" element={<Error404 />} />

            </Routes>
        </Router>
    );
};

export default AppRoutes;
