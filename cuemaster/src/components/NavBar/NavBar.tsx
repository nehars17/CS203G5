import React, { useState, useEffect } from 'react';
import { Navbar, Nav, Container, NavDropdown, Toast, Image } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { isAuthenticated, getUserIdFromToken } from '../authUtils';
import API from '../../services/api';
import './NavBar.css';

// Define the expected response structure
interface UserResponse {
  username: string;
}

const NavBar: React.FC = () => {
  const [username, setUsername] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [showToast, setShowToast] = useState<boolean>(false);
  const isUserAuthenticated = isAuthenticated();
  const userId = getUserIdFromToken(); // Extract the userId from the token
  const navigate = useNavigate();

  const handleLogout = async () => {
    localStorage.removeItem('token');
    await fetch('http://localhost:8080/logout', {
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
    const fetchUserData = async () => {
      if (isUserAuthenticated) {
        try {
          // Fetch the username directly as a string from the backend
          const response = await API.get<string>(`/userName/${userId}`);
          setUsername(response.data); // Set the username directly as a string
        } catch (error) {
          console.error('Error fetching user data:', error);
          setUsername('Error fetching username');
        } finally {
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [isUserAuthenticated, userId]);

  // Construct the profile photo URL based on the userId
  const profilePhotoUrl = `http://localhost:8080/profilePhotos/ProfilePhoto_${userId}.jpg`;

  return (
    <>
      <Navbar className="custom-navbar" expand="lg">
        <Container>
          <Navbar.Brand href="/">CueMaster</Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="me-auto">
                <>
                  <Nav.Link href="/home">Home</Nav.Link>
                  <Nav.Link href="/profiles">ProfileDashboard</Nav.Link>
                  <Nav.Link href="/leaderboard">Leaderboard</Nav.Link>
                  <Nav.Link href="/tournaments">Tournaments</Nav.Link>
                </>
            </Nav>
  
            {/* Links for non-logged-in users */}
            {!isUserAuthenticated && (
              <Nav className="ms-auto"> {/* Add 'ms-auto' to push to the right */}
                <NavDropdown title="Registration" id="basic-nav-dropdown">
                  <NavDropdown.Item href="/playerRegistration">Player Registration</NavDropdown.Item>
                  <NavDropdown.Item href="/organiserRegistration">Organiser Registration</NavDropdown.Item>
                </NavDropdown>
                <Nav.Link href="/login">Login</Nav.Link>
              </Nav>
            )}
  
            {/* Links for logged-in users */}
            {isUserAuthenticated && (
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
                      <span className="nav-username">{username}</span> {/* Username next to the photo */}
                    </>
                  )}
                </Nav.Link>
                <NavDropdown title="Account" id="basic-nav-dropdown">
                  <NavDropdown.Item href="/account">My Account</NavDropdown.Item>
                  <NavDropdown.Item href={`/profile/${userId}`}>My Profile</NavDropdown.Item>
                  <NavDropdown.Item href="#" onClick={handleLogout}>Logout</NavDropdown.Item>
                </NavDropdown>
              </Nav>
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
}

export default NavBar;