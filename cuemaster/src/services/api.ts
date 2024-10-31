import axios from 'axios';

const API = axios.create({
    baseURL: 'http://localhost:8080/', // Your backend URL
});

// For setting up authorization token in headers
export const setAuthToken = (token: string | null) => {
    if (token) {
        API.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete API.defaults.headers.common['Authorization'];
    }
};

export const deleteTournament = async (id: number) => {
    try {
        const authToken = localStorage.getItem('authToken'); // Or wherever you store your token
        const response = await API.delete(`/tournaments/${id}`, {
            headers: {
                Authorization: `Bearer ${authToken}`,
            },
            withCredentials: true,
        });
        console.log('Deletion response:', response); // Log the response
    } catch (error) {
        console.error('Error deleting tournament:', error);
        throw error; // Throw error to be caught in handleDelete
    }
};

export const joinTournament = async (tournamentId: number, playerId: number) => {
    try {
        const authToken = localStorage.getItem('authToken'); // Or wherever you store your token
        const response = await API.post(`/tournaments/${tournamentId}/join`, { playerId }, {
            headers: {
                Authorization: `Bearer ${authToken}`,
            },
            withCredentials: true,
        });
        console.log('Join response:', response); // Log the response
        return response; // Return the response to be used in the component
    } catch (error) {
        console.error('Error joining tournament:', error);
        throw error; // Throw error to be caught in handleJoin
    }
};

export const leaveTournament = async (tournamentId: number, playerId: number) => {
    try {
        const authToken = localStorage.getItem('authToken'); // Or wherever you store your token
        const response = await API.post(`/tournaments/${tournamentId}/leave`, { playerId }, {
            headers: {
                Authorization: `Bearer ${authToken}`,
            },
            withCredentials: true,
        });
        console.log('Leave response:', response); // Log the response
        return response; // Return the response to be used in the component
    } catch (error) {
        console.error('Error leaving tournament:', error);
        throw error; // Throw error to be caught in handleLeave
    }
};

export default API;
