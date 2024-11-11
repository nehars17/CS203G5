import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Login from './components/User/Login';
import RegisterPlayer from './components/User/RegisterPlayer';
import RegisterOrganiser from './components/User/RegisterOrganiser';
import CreateProfile from './components/Profile/CreateProfile';
import ProfileDashboard from './components/Profile/ProfileDashboard';
import EmailAuthForm from './components/User/EmailAuthForm';
import Profile from './components/Profile/Profile';
import Leaderboard from './components/Leaderboard/Leaderboard';
import NavBar from './components/NavBar/NavBar';
import Error404 from './pages/Error404';
import Forbidden403 from './pages/Forbidden403';
import PrivateRoute from './components/PrivateRoute';
import AdminDashboard from './components/Admin/AdminDashboard';
import Home from './components/Home/Home';
import ResetPassword from './components/User/ResetPassword';
import ForgotPassword from './components/User/ForgotPassword';
import AccountActivated from './components/User/AccountActivated';

import About from './pages/About';
import Account from './components/Account/Account';
import { isAuthenticated, getUserIdFromToken, getUserRole } from './components/authUtils';
import { Navigate } from 'react-router-dom';
import EditProfile from './components/Profile/EditProfile';

//tournament
import Tournament from './components/Tournament/Tournament';
import CreateTournament from './components/Tournament/CreateTournament';
import UpdateTournament from './components/Tournament/UpdateTournament';

//Match
import CreateMatch from './components/Matches/CreateMatch';
import Matches from './components/Matches/Matches';

// Define the AppRoutes component
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
                <Route path="/profiles" element={<ProfileDashboard />} />
                <Route path="/profile/:userId" element={<Profile />} />
                
                {/*Tournament Routes */}
                <Route path="/tournaments" element={<Tournament />} />
                <Route path="/tournaments/create-tournament" element={<CreateTournament />} />
                <Route path="/tournaments/update-tournament/:id" element={<UpdateTournament />}/>


                <Route path="/leaderboard" element={<Leaderboard />} />
             
               
                <Route path="/emailauth" element={<EmailAuthForm />} />
                <Route path="/activateaccount" element={<AccountActivated />} />
                <Route path="/resetPassword" element={<ResetPassword />} />
                <Route path="/forgotPassword" element={<ForgotPassword />} />
                <Route path="/403" element={<Forbidden403 />} />
                <Route path="*" element={<Error404 />} />
             
                {/*Match Management route*/}
                <Route path="/matches" element={<CreateMatch />} />
                {/*View matches */}
                <Route path="/tournaments/:tournamentId/matches" element={<Matches />} />
                <Route path="/tournaments/:id/edit" element={<UpdateTournament />} />

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
                    path="/adminDashboard"
                    element={
                        isUserAuthenticated && userId === 1 ? ( // Check if authenticated and user_id is '1'
                            <AdminDashboard />
                        ) : (
                            <Navigate to="/login" />
                        )
                    }
                />
                <Route
                    path="/ProfileCreation/:userId"
                    element={
                        <PrivateRoute isAuthenticated={isUserAuthenticated}>
                            <CreateProfile />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/ProfileAmendment/:userId"
                    element={
                        <PrivateRoute isAuthenticated={isUserAuthenticated}>
                            <EditProfile />
                        </PrivateRoute>
                    }
                />
                <Route
                    path="/account"
                    element={
                        <PrivateRoute isAuthenticated={isUserAuthenticated}>
                            <Account />
                        </PrivateRoute>
                    }
                />
            </Routes>
        </Router>
    );
};

export default AppRoutes;
