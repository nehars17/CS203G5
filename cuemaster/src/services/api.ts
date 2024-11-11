import axios from 'axios';

const API = axios.create({
    baseURL: 'https://cuemaster-g0dvbuawbuczcbbn.canadacentral-01.azurewebsites.net/', // Your backend URL
});

// For setting up authorization token in headers
export const setAuthToken = (token: string | null) => {
    if (token) {
        API.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete API.defaults.headers.common['Authorization'];
    }
};

export default API;
