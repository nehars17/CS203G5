import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Container, Alert } from 'react-bootstrap';
import { GoogleLogin } from '@react-oauth/google';
import ReCAPTCHA from 'react-google-recaptcha'; 
import useRecaptcha from './useRecaptcha';
import config from '../../config';
import { isAuthenticated, getUserIdFromToken, getUserRole } from '../../components/authUtils';

const Login: React.FC = () => {
  const { captchaToken, recaptchaRef, handleRecaptcha } = useRecaptcha();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false); // Track form submission state
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!captchaToken) {
      setError('Please complete CAPTCHA');
      return;
    }
    setIsSubmitting(true); // Disable form submission during the process

    try {
      const res = await fetch(`${config.apiBaseUrl}/normallogin`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: email,
          password: password,
          recaptchaToken: captchaToken,
        }),
      });

      if (!res.ok) {
        const errorData = await res.json();
        setError(errorData.message || 'Login failed');
        throw new Error('Login failed');
      }

      const data = await res.json();
      if (data.message) {
        setError(data.message);
      } else {
        navigate('/emailauth', { state: { data } });
      }
    } catch (error) {
      console.error('Login failed:', error);
      setError('Login failed, please check your credentials and try again.');
    } finally {
      setIsSubmitting(false); // Re-enable the form
    }
  };

  const onSuccess = async (response: any) => {
    try {
      const userInfoResponse = await fetch(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${response.credential}`);
      const userInfo = await userInfoResponse.json();
      const email = userInfo.email;

      const res = await fetch(`${config.apiBaseUrl}/googlelogin`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          tokenId: response.credential,
          email: email,
        }),
      });

      const data = await res.json();
      if (data.message) {
        setError(data.message);
      }

      if (data.token) {
        localStorage.setItem('token', data.token); // Store token

        // Fetch user profile based on role
        const userRole = data.role;
        let profileData = null;

        // If the profile is needed, fetch it
        if (userRole === 'ROLE_PLAYER' || userRole === 'ROLE_ORGANISER') {
          const profileRes = await fetch(`${config.apiBaseUrl}/profile/${data.userId}`, {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
            },
          });

          if (profileRes.ok) {
            profileData = await profileRes.json();
          }
        }

        // Redirect based on the role and profile status
        if (userRole === 'ROLE_PLAYER') {
          if (!profileData) {
            window.location.href = '/ProfileCreation';
          } else {
            window.location.href = '/home';
          }
        } else if (userRole === 'ROLE_ORGANISER') {
          if (!profileData) {
            window.location.href = '/ProfileCreation';
          } else {
            window.location.href = '/home';
          }
        }
      } else {
        setError('Google login failed.');
      }
    } catch (error) {
      console.error('Error during Google login:', error);
      setError('Google login failed, please try again.');
    }
  };

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: '25rem' }}>
        <Card.Body>
          <h3 className="text-center mb-4">Login</h3>
          {error && <Alert variant="danger">{error}</Alert>} {/* Show error alert */}
          <Form onSubmit={handleSubmit}>
            <Form.Group controlId="formBasicEmail">
              <Form.Label>Email address</Form.Label>
              <Form.Control
                type="email"
                placeholder="Enter email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isSubmitting} // Disable input during submission
              />
            </Form.Group>
            <Form.Group controlId="formBasicPassword" className="mt-3">
              <Form.Label>Password</Form.Label>
              <Form.Control
                type="password"
                placeholder="Password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isSubmitting} // Disable input during submission
              />
            </Form.Group>
            <Form.Group controlId="recaptcha" className="mt-3">
              <ReCAPTCHA
                ref={recaptchaRef}
                sitekey="6LdKu3kqAAAAAAeXkISFRFa_DokCrNUxlr-Q_m2H" //captcha site key
                onChange={handleRecaptcha}
              />
            </Form.Group>

            <Button variant="primary" type="submit" className="w-100 mt-4" disabled={isSubmitting}>
              {isSubmitting ? 'Submitting...' : 'Login'}
            </Button>
          </Form>
          <div className="mt-3">
            <GoogleLogin
              onSuccess={onSuccess}
              onError={() => console.error('Google login failed')} // Handle Google login failure
            />
          </div>
          <br />
          <a href="/forgotPassword">Forgot Password?</a>

          <div className="text-center mt-3">
            <small>
              Don't have an account? <br />
              <a href="/playerRegistration">Player Registration</a>
              <br />
              <a href="/organiserRegistration">Organiser Registration</a>
            </small>
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default Login;
