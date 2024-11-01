import jwt from 'jsonwebtoken';

// Secret key used for signing the JWT
const secretKey = '3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b'; // Use the same secret used to sign the JWT

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
