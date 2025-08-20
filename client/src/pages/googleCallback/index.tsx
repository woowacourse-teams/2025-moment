import { getProfile } from '@/features/auth/api/useProfileQuery';
import { useToast } from '@/shared/hooks/useToast';
import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';

export default function GoogleCallbackPage() {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');

    if (success === 'true') {
      showSuccess('구글 로그인에 성공했습니다.');
      queryClient.setQueryData(['checkIfLoggedIn'], true);
      queryClient.prefetchQuery({ queryKey: ['profile'], queryFn: getProfile });
    } else {
      showError('구글 로그인에 실패했습니다. 다시 시도해주세요.');
    }
    navigate('/', { replace: true });
  }, []);

  return <div>Google Callback Page</div>;
}
