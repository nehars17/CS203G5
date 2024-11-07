import jwt from 'jsonwebtoken';

// Secret key used for signing the JWT
const secretKey = 'AFx_qI6V3f0SZkLoV7t7ztuiQkbuNvydBc0Xybd09tRKlxV-isa5NVrZdK9gPSVZ0hv3AbzIeRuCMeF7r2gAUDNvPUT5wuzDmYrX0uGuPAbd3qRxfumR9mncTP_chCletXF9he2TlFTk'; // Use the same secret used to sign the JWT

interface DecodedToken {
    sub: string; // Adjust according to your token structure
    exp: number; // Token expiration time
    user_id:number; //user_id
    role:string;
    // authority:string; // Add other fields from your JWT payload as necessary
}

export const isAuthenticated = (): boolean => {
    const jwtToken = localStorage.getItem('token');
    if (!jwtToken) return false;
    try {
        // Decode the token without verifying it
        const payload = jwt.decode(jwtToken) as DecodedToken;

        // Check if the token is expired
        if (payload && payload.exp * 1000 < Date.now()) {
            localStorage.removeItem('token'); // Remove expired token
            console.log("Im CALLED, i expired");
            return false;
        }

        return true;
    } catch (error) {
        console.error('Failed to decode token', error);
        return false;
    }
};

export const getUserIdFromToken = (): number | null => {
    const jwtToken = localStorage.getItem('token');
  
    if (!jwtToken) {
        return null; // Return null if there's no token
    }
  
    try {
        const decoded = jwt.decode(jwtToken) as DecodedToken;
        console.log(decoded.user_id)
        return decoded.user_id; // Return user ID from the decoded token
    } catch (error) {
        console.error('Failed to decode token', error);
        return null; // Return null if decoding fails
    }
};


export const getUserRole = (): string | null => {
    const jwtToken = localStorage.getItem('token');
  
    if (!jwtToken) {
        return null; // Return null if there's no token
    }
  
    try {
        const decoded = jwt.decode(jwtToken) as DecodedToken;
        console.log(decoded.role)
        return decoded.role; // Return user ID from the decoded token
    } catch (error) {
        console.error('Failed to decode token', error);
        return null; // Return null if decoding fails
    }
};

export const getAuthToken = () => {
    return localStorage.getItem('token'); // Adjust according to your setup
};

