import React from 'react';
import AppRoutes from './routes'; // Import your routes
// src/index.js or src/App.js
import 'bootstrap/dist/css/bootstrap.min.css';

const App: React.FC = () => {
    return (
        <div>
            <AppRoutes />  {/* Render the routing logic */}
        </div>
    );
}

export default App;
