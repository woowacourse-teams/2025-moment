import { ROUTES } from '@/app/routes/routes';
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

export default function GoogleCallbackPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const success = searchParams.get('success') === 'true';
    const error = searchParams.get('error');

    if (success) {
      navigate(ROUTES.ROOT);
    } else {
      console.log('Google 로그인 실패', error);
      navigate(ROUTES.LOGIN);
    }
  }, [searchParams, navigate]);

  return (
    <div>
      <h1>로그인 처리 중...</h1>
    </div>
  );
}
