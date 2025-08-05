import { ROUTES } from '@/app/routes/routes';
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { useToast } from '@/shared/hooks/useToast';

export default function GoogleCallbackPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  useEffect(() => {
    const success = searchParams.get('success') === 'true';
    const error = searchParams.get('error');

    if (success) {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      showSuccess('Google 로그인에 성공했습니다!');
      navigate(ROUTES.ROOT);
    } else {
      console.error('Google 로그인 실패:', error);
      showError('Google 로그인에 실패했습니다.');
      navigate(ROUTES.LOGIN);
    }
  }, [searchParams, navigate, queryClient, showSuccess, showError]);

  return (
    <div style={{ textAlign: 'center', padding: '2rem' }}>
      <h1>Google 로그인 처리 중...</h1>
    </div>
  );
}
