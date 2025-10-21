import { getProfile } from '@/features/auth/api/useProfileQuery';
import { useToast } from '@/shared/hooks/useToast';
import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import { requestFCMPermissionAndToken } from '@/shared/utils/firebase';
import { registerFCMToken } from '@/shared/notifications/useInitializeFCM';

export default function GoogleCallbackPage() {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const handleGoogleCallback = async () => {
      const urlParams = new URLSearchParams(window.location.search);
      const success = urlParams.get('success');

      if (success === 'true') {
        showSuccess('구글 로그인에 성공했습니다.');
        queryClient.setQueryData(['checkIfLoggedIn'], true);
        await queryClient.prefetchQuery({ queryKey: ['profile'], queryFn: getProfile });

        try {
          const token = await requestFCMPermissionAndToken();
          if (token) {
            await registerFCMToken(token);
          }
        } catch (error) {
          console.error('[FCM] 구글 로그인 후 토큰 등록 실패:', error);
        }
      } else {
        showError('구글 로그인에 실패했습니다. 다시 시도해주세요.');
      }
      navigate('/', { replace: true });
    };

    handleGoogleCallback();
  }, []);

  return <div>Google Callback Page</div>;
}
