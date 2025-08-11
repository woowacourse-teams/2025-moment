import { useAuthContext } from '@/features/auth/context/useAuthContext';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';

export default function GoogleCallbackPage() {
  const { setIsLoggedIn } = useAuthContext();
  const navigate = useNavigate();

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');

    if (success === 'true') {
      setIsLoggedIn(true);
    } else {
      setIsLoggedIn(false);
      console.error('Google login failed. Please try again.');
    }
    navigate('/');
  }, []);

  return <div>Google Callback Page</div>;
}
