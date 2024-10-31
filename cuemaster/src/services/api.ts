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

export default API;
