import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Login from './components/User/Login';
import RegisterPlayer from './components/User/RegisterPlayer';
import RegisterOrganiser from './components/User/RegisterOrganiser';
import EmailAuthForm from './components/User/EmailAuthForm';

import Profile from './components/Profile/Profile';
import Matches from './components/Matches/Matches';
import Leaderboard from './components/Leaderboard/Leaderboard';
import Tournament from './components/Tournament/Tournament';
import NavBar from './components/NavBar/NavBar';
import Error404 from './pages/Error404';
import PrivateRoute from './components/PrivateRoute';
import AdminDashboard from './components/Admin/AdminDashboard';
import Home from './components/Home/Home';
import ResetPassword from './components/User/ResetPassword';
import ForgotPassword from './components/User/ForgotPassword';
import AccountActivated from './components/User/AccountActivated';

import About from './pages/About';
import { isAuthenticated, getUserIdFromToken, getUserRole } from './components/authUtils';
import { Navigate } from 'react-router-dom';

const AppRoutes: React.FC = () => {
    const isUserAuthenticated = isAuthenticated();
    const userId = getUserIdFromToken();
    const isRole = getUserRole();

    return (
        <Router>
            <NavBar />
            <Routes>
                <Route path="/" element={<About />} />
                <Route path="/login" element={<Login />} />
                <Route path="/playerRegistration" element={<RegisterPlayer />} />
                <Route path="/organiserRegistration" element={<RegisterOrganiser />} />
                <Route path="/tournaments" element={<Tournament />} />
                <Route path="/leaderboard" element={<Leaderboard />} />
                <Route path="/matches" element={<Matches />} />
                <Route path="/emailauth" element={<EmailAuthForm />} />
                <Route path="/activateaccount" element={<AccountActivated />} />
                <Route path="/resetPassword" element={<ResetPassword />} />
                <Route path="/forgotPassword" element={<ForgotPassword />} />

                <Route path="*" element={<Error404 />} />
             


                {/* Private Routes */}
                <Route
                    path="/home"
                    element={
                        <PrivateRoute isAuthenticated={isUserAuthenticated}>
                            <Home />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/playerProfile"
                    element={
                        <PrivateRoute isAuthenticated={isUserAuthenticated && isRole === "ROLE_PLAYER"}>
                            <Profile />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/organiserProfile"
                    element={
                        <PrivateRoute isAuthenticated={isUserAuthenticated}>
                            <Profile />
                        </PrivateRoute>
                    }
                />
               <Route
                    path="/adminDashboard"
                    element={
                        isUserAuthenticated && userId === 1 ? ( // Check if authenticated and user_id is '1'
                            <AdminDashboard />
                        ) : (
                            <Navigate to="/login" />
                        )
                    }
                />
            </Routes>
        </Router>
    );
};

export default AppRoutes;
