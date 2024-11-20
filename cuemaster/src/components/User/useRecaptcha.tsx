import { useState, useRef } from 'react';
import ReCAPTCHA from 'react-google-recaptcha';

const useRecaptcha = () => {
  const [captchaToken, setCaptchaToken] = useState<string | null>(null);
  const recaptchaRef = useRef<ReCAPTCHA | null>(null);

  const handleRecaptcha = (value: string | null) => {
    setCaptchaToken(value); // This will store the token for the reCAPTCHA
  };

  return {
    captchaToken,
    recaptchaRef,
    handleRecaptcha
  };
};

export default useRecaptcha;
