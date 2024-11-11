import React from 'react';
import AppRoutes from './routes'; // Import your routes
// src/index.js or src/App.js
import 'bootstrap/dist/css/bootstrap.min.css';
import { GoogleOAuthProvider } from '@react-oauth/google';

// Define the App component
// Use the GoogleOAuthProvider component to wrap the AppRoutes component
const App: React.FC = () => {
    return (
        <GoogleOAuthProvider clientId="643146770456-qeusj14u53puh4bi1hhi2t8g21qhh1hc.apps.googleusercontent.com">
          <div>
            <AppRoutes />
          </div>
        </GoogleOAuthProvider>
    );
}


export default App;
