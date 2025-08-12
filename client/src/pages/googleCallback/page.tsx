import { useToast } from '@/shared/hooks/useToast';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';

export default function GoogleCallbackPage() {
  const { showSuccess, showError } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');

    if (success === 'true') {
      showSuccess('구글 로그인에 성공했습니다.');
    } else {
      showError('구글 로그인에 실패했습니다. 다시 시도해주세요.');
    }
    navigate('/');
  }, []);

  return <div>Google Callback Page</div>;
}
