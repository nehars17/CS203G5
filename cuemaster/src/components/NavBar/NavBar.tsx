import React from 'react';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { getUserRole, isAuthenticated } from '../authUtils'; // Import your authentication check function

const NavBar: React.FC = () => {
    const isUserAuthenticated = isAuthenticated();
    const userRole = getUserRole();

    return (
        <Navbar bg="light" expand="lg">
            <Container>
                <Navbar.Brand href="/">CueMaster</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                       
                        {/* All users can view*/}
                        <Nav.Link href="/">Home</Nav.Link>
                        <Nav.Link href="/tournaments">Tournaments</Nav.Link>
                        <Nav.Link href="/leaderboard">Leaderboard</Nav.Link>
                        <Nav.Link href="/matches">Matches</Nav.Link>

                        {isUserAuthenticated ? (
                            <>
                                {/* Admin-only links */}
                                {userRole === "ROLE_ADMIN" && (
                                    <>
                                        <Nav.Link href="/adminDashboard">Admin Dashboard</Nav.Link>
                                        <Nav.Link href="/manageUsers">Manage Users</Nav.Link>
                                    </>  
                                )}

                                {/* Player-only links */}
                                {userRole === "ROLE_PLAYER" && (
                                    <>  <Nav.Link href="/home">Home</Nav.Link>
                                        <Nav.Link href="/playerProfile">Profile</Nav.Link>
                                        {/* <Nav.Link href="/playerDashboard">Player Dashboard</Nav.Link> */}
                                    </>
                                )}

                                {/* Organizer-only links */}
                                {userRole === "ROLE_ORGANISER" && (
                                    <>
                                        <Nav.Link href="/home">Home</Nav.Link>
                                        <Nav.Link href="/organiserProfile">Profile</Nav.Link>
                                        {/* <Nav.Link href="/organiserDashboard">Organiser Dashboard</Nav.Link> */}
                                    </>
                                )}
                            </>
                        ) : (
                            // Authentication Links for non-authenticated users
                            <>
                                <Nav.Link href="/playerRegistration">Player Registration</Nav.Link>
                                <Nav.Link href="/organiserRegistration">Organiser Registration</Nav.Link>
                                <Nav.Link href="/login">Login</Nav.Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default NavBar;
