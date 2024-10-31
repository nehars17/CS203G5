import React from 'react';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { isAuthenticated } from '../authUtils'; // Import your authentication check function

const NavBar: React.FC = () => {
    const isUserAuthenticated = isAuthenticated();

    return (
        <Navbar bg="light" expand="lg">
            <Container>
                <Navbar.Brand href="/">CueMaster</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        {isUserAuthenticated ? (
                            // Links for authenticated users
                            <>
                                <Nav.Link href="/home">Home</Nav.Link>
                                <Nav.Link href="/playerProfile">Profile</Nav.Link>
                                <Nav.Link href="/tournaments">Tournaments</Nav.Link>
                            </>
                        ) : (
                            // Links for non-authenticated users
                            <>
                                <Nav.Link href="/">Home</Nav.Link>
                                <Nav.Link href="/tournaments">Tournaments</Nav.Link>
                                <Nav.Link href="/leaderboard">leaderboard</Nav.Link>
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
