import React from 'react';
import { Image, ListGroup } from 'react-bootstrap';
import { getAuthToken } from '../authUtils';

interface PlayerCardProps {
    playerName: string;
}

const PlayerCard: React.FC<PlayerCardProps> = ({ playerName }) => {
    return (
        <ListGroup horizontal className="align-items-center">
            <Image roundedCircle src="https://via.placeholder.com/50" alt={playerName} className="mr-2" />
            <ListGroup.Item>
                <div>Name: {playerName}</div>
                <small>Score: TBD</small>
            </ListGroup.Item>
        </ListGroup>
    );
};

export default PlayerCard;