import { getProfile } from '@/features/auth/api/useProfileQuery';
import { toast } from '@/shared/store/toast';
import { queryKeys } from '@/shared/lib/queryKeys';
import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';

export default function GoogleCallbackPage() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  useEffect(() => {
    const handleGoogleCallback = async () => {
      const urlParams = new URLSearchParams(window.location.search);
      const success = urlParams.get('success');

      if (success === 'true') {
        toast.success('구글 로그인에 성공했습니다.');
        queryClient.setQueryData(queryKeys.auth.checkLogin, true);
        await queryClient.prefetchQuery({ queryKey: queryKeys.auth.profile, queryFn: getProfile });
      } else {
        toast.error('구글 로그인에 실패했습니다. 다시 시도해주세요.');
      }
      navigate('/', { replace: true });
    };

    handleGoogleCallback();
  }, []);

  return <div>Google Callback Page</div>;
}
