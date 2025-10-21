import { useState, useEffect } from 'react';
import { requestFCMPermissionAndToken } from '@/shared/utils/firebase';
import { Bell, BellOff, AlertCircle } from 'lucide-react';
import * as S from './NotificationSettings.styles';
import { registerFCMToken } from '@/shared/notifications/registerFCMToken';

type PermissionStatus = 'default' | 'granted' | 'denied';

export const NotificationSettings = () => {
  const [permission, setPermission] = useState<PermissionStatus>('default');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 초기 로드 시 권한 확인
  useEffect(() => {
    if ('Notification' in window) {
      setPermission(Notification.permission);
    }
  }, []);

  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible' && 'Notification' in window) {
        setPermission(Notification.permission);
      }
    };

    const handleFocus = () => {
      if ('Notification' in window) {
        setPermission(Notification.permission);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('focus', handleFocus);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('focus', handleFocus);
    };
  }, []);

  const handleToggleNotification = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const token = await requestFCMPermissionAndToken();

      if (token) {
        await registerFCMToken(token);
        setPermission('granted');
      } else {
        // 사용자가 거부했거나 지원하지 않는 경우
        setPermission(Notification.permission);
        if (Notification.permission === 'denied') {
          setError('알림 권한이 거부되었습니다. 브라우저 설정에서 변경해주세요.');
        }
      }
    } catch (err) {
      console.error('[FCM] 알림 설정 오류:', err);
      setError('알림 설정 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusInfo = () => {
    switch (permission) {
      case 'granted':
        return {
          icon: <Bell size={20} />,
          text: '푸시 알림 켜짐',
          description: '새로운 소식을 실시간으로 받아볼 수 있습니다.',
          color: '#4caf50',
          canToggle: false,
        };
      case 'denied':
        return {
          icon: <BellOff size={20} />,
          text: '푸시 알림 차단됨',
          description: '브라우저 설정에서 알림 권한을 허용해주세요.',
          color: '#f44336',
          canToggle: false,
        };
      default:
        return {
          icon: <Bell size={20} />,
          text: '푸시 알림 받기',
          description: '새로운 댓글, 좋아요 등의 알림을 받아보세요.',
          color: '#666',
          canToggle: true,
        };
    }
  };

  const statusInfo = getStatusInfo();

  return (
    <S.Container>
      <S.Header>
        <S.Title>알림 설정 안내</S.Title>
      </S.Header>

      <S.NotificationCard>
        <S.NotificationInfo>
          <S.IconWrapper color={statusInfo.color}>{statusInfo.icon}</S.IconWrapper>
          <S.TextWrapper>
            <S.StatusText>{statusInfo.text}</S.StatusText>
            <S.Description>{statusInfo.description}</S.Description>
          </S.TextWrapper>
        </S.NotificationInfo>

        {statusInfo.canToggle && (
          <S.ToggleButton onClick={handleToggleNotification} disabled={isLoading}>
            {isLoading ? '설정 중...' : '알림 켜기'}
          </S.ToggleButton>
        )}

        {permission === 'granted' && (
          <S.InfoBox>
            알림이 활성화되어 있습니다. 브라우저 및 기기 설정에서 변경할 수 있습니다.
          </S.InfoBox>
        )}

        {permission === 'denied' && (
          <S.HelpText>
            <AlertCircle size={14} />
            iOS: 설정 → Safari → 웹사이트 설정 또는 설정 → 알림
            <br />
            Android: 브라우저 설정 → 사이트 설정 → 알림
          </S.HelpText>
        )}

        {error && <S.ErrorText>{error}</S.ErrorText>}
      </S.NotificationCard>
    </S.Container>
  );
};
