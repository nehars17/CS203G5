import React, { useState, useEffect } from 'react';
import { Navbar, Nav, Container, NavDropdown, Toast, Image } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { isAuthenticated, getUserIdFromToken } from '../authUtils';
import API from '../../services/api';
import './NavBar.css';
import config from '../../config';

interface Profile {
  id: number;
  profilephotopath: string;
}

// Define the expected response structure for the username
interface UserResponse {
  username: string;
}

const NavBar: React.FC = () => {
  const [username, setUsername] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [showToast, setShowToast] = useState<boolean>(false);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [error, setError] = useState<string | null>(null);
  
  const isUserAuthenticated = isAuthenticated();
  const userId = getUserIdFromToken();
  const navigate = useNavigate();

  const handleLogout = async () => {
    localStorage.removeItem('token');
    await fetch(`${config.apiBaseUrl}/logout`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    setShowToast(true);
    setTimeout(() => {
      navigate('/');
    }, 1500);
  };

  useEffect(() => {
    const fetchProfile = async () => {
      if (userId && isUserAuthenticated) {
        try {
          const response = await API.get<Profile>(`/profile/${userId}`);
          setProfile(response.data);

          // Fetch the username directly
          const userResponse = await API.get<UserResponse>(`/userName/${userId}`);
          setUsername(userResponse.data.username);
        } catch (err) {
          const errorMessage = (err as any)?.response?.data?.message || 'Failed to load profile data';
          setError(errorMessage);
          console.error(err);
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [isUserAuthenticated, userId]);

  // Construct the profile photo URL
  const profilePhotoUrl = profile?.profilephotopath 
    ? `${config.apiBaseUrl}/profilePhotos/${profile.profilephotopath}`
    : `${config.apiBaseUrl}/profilePhotos/default.jpg`; // Default image if none exists

  return (
    <>
      <Navbar className="custom-navbar" expand="lg">
        <Container>
          <Navbar.Brand href="/">CueMaster</Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="me-auto">
              <Nav.Link href="/home">Home</Nav.Link>
              <Nav.Link href="/profiles">ProfileDashboard</Nav.Link>
              <Nav.Link href="/leaderboard">Leaderboard</Nav.Link>
              <Nav.Link href="/tournaments">Tournaments</Nav.Link>
              <Nav.Link href="/matches/tournament/id">Matches</Nav.Link>
            </Nav>

            {/* Links for non-logged-in users */}
            {!isUserAuthenticated && (
              <Nav className="ms-auto">
                <NavDropdown title="Registration" id="basic-nav-dropdown">
                  <NavDropdown.Item href="/playerRegistration">Player Registration</NavDropdown.Item>
                  <NavDropdown.Item href="/organiserRegistration">Organiser Registration</NavDropdown.Item>
                </NavDropdown>
                <Nav.Link href="/login">Login</Nav.Link>
              </Nav>
            )}

            {/* Links for logged-in users */}
            {isUserAuthenticated && (
              <>
                {userId === 1 && (
                  <Nav className="ms-auto">
                    <Nav.Link href="/adminDashboard">Admin Dashboard</Nav.Link>
                  </Nav>
                )}
                <Nav className="align-items-center">
                  <Nav.Link className="profile-nav-container">
                    {loading ? 'Loading...' : (
                      <>
                        {profilePhotoUrl && (
                          <Image
                            src={profilePhotoUrl}
                            className="profile-nav-image"
                            alt="Profile"
                          />
                        )}
                        <span className="nav-username">{username}</span>
                      </>
                    )}
                  </Nav.Link>
                  <NavDropdown title="Account" id="basic-nav-dropdown">
                    <NavDropdown.Item href="/account">My Account</NavDropdown.Item>
                    <NavDropdown.Item href={`/profile/${userId}`}>My Profile</NavDropdown.Item>
                    <NavDropdown.Item href="#" onClick={handleLogout}>Logout</NavDropdown.Item>
                  </NavDropdown>
                </Nav>
              </>
            )}
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <Toast
        show={showToast}
        onClose={() => setShowToast(false)}
        delay={3000}
        autohide
        className="position-fixed bottom-0 end-0 m-3"
      >
        <Toast.Body>Successfully logged out!</Toast.Body>
      </Toast>
    </>
  );
};

export default NavBar;
