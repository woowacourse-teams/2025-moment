import { Button } from '@/shared/ui/button/Button';
import { useNavigate } from 'react-router';

interface AuthButtonProps {
  onClick?: () => void;
}

export const AuthButton = ({ onClick }: AuthButtonProps) => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    onClick?.();
    navigate('/login');
  };

  return <Button title="로그인" onClick={handleLoginClick} variant="primary" />;
};
