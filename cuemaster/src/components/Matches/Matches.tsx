import React, { useEffect, useState } from 'react';
import { Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import API from '../../services/api';
import MatchCard from './MatchCard';
// import { getAuthToken } from '../authUtils';


// Define a type for the match item
interface Match {
    player1: string;
    player2: string;
    date: string; 
    status: string;
    winner: string;
}

const Matches: React.FC = () => {
    const [matches, setMatches] = useState<Match[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await API.get<Match[]>('/matches'); // Specify the expected type here
                setMatches(response.data);
            } catch (error) {
                console.error('Error fetching matches', error);
                setError('Failed to fetch matches. Please try again later.');
            } finally {
                setLoading(false); // Set loading to false after the fetch is complete
            }
        };
    
        fetchMatches();
    }, []);

    //spinny wheel
    if (loading) {
        return <Spinner animation="border" variant="primary" /> 
    }

    // Error message
    if (error) {
    return <Alert variant="danger">{error}</Alert>;
    }

    return (
        <Container>
            <h4>Tournament's Matches</h4>
            <Row>
                {matches.map((match, index) => (
                    <Col  xs={12} md={6} lg={4} key={index}>
                        <MatchCard 
                            player1={match.player1}
                            player2={match.player2}
                            status={match.status}
                            winner = {match.winner}
                        />
                    </Col>
                ))}
            </Row>
        </Container>
    );
};

export default Matches;