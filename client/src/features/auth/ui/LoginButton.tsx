import { Button } from '@/shared/ui/button/Button';
import { useNavigate } from 'react-router';

interface LoginButtonProps {
  onClick?: () => void;
}

export const LoginButton = ({ onClick }: LoginButtonProps) => {
  const navigate = useNavigate();

  // 로그아웃 로직 추가

  const handleLoginClick = () => {
    onClick?.();
    navigate('/login');
  };

  return <Button title="로그인" onClick={handleLoginClick} variant="primary" />;
};
