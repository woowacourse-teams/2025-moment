import { useNavigate } from 'react-router';

export function useNavigateHandler(path: string) {
  const navigate = useNavigate();

  const handlePagination = () => {
    navigate(path);
  };

  return handlePagination;
}
