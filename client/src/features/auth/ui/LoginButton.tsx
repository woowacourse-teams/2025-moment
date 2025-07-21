import { Button } from '@/shared/ui/button/Button';
import { useNavigate } from 'react-router';

export const LoginButton = () => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    navigate('/login');
  };

  return <Button title="로그인" onClick={handleLoginClick} variant="primary" />;
};
