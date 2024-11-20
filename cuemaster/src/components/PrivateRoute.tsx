import React from 'react';
import { Navigate } from 'react-router-dom';

// Define the PrivateRouteProps interface
interface PrivateRouteProps {
    isAuthenticated: boolean;
    children: React.ReactNode; // Use ReactNode to allow any valid React child
}

  
const PrivateRoute: React.FC<PrivateRouteProps> = ({
    isAuthenticated,
    children,
}) => {
    // Check both authentication status and if the user ID is valid (non-null)
    if (isAuthenticated) {
        return <>{children}</>; // Render the child components

    }
    else{
    // Redirect to the login page if not authenticated or user ID is invalid
       return <Navigate to="/login" />;
    }
};

export default PrivateRoute;
