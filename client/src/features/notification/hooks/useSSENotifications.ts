import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { subscribeNotifications } from '../api/subscribeNotifications';
import { NotificationItem, NotificationResponse } from '../types/notifications';
import { SSENotification } from '../types/sseNotification';
import { useToast } from '@/shared/hooks/useToast';
import { useProfileQuery } from '@/features/auth/hooks/useProfileQuery';

export const useSSENotifications = () => {
  const queryClient = useQueryClient();
  const { showError, showSuccess } = useToast();
  const { data: profile, isLoading, isSuccess } = useProfileQuery();

  const isLoggedIn = isSuccess && profile && !isLoading;

  useEffect(() => {
    if (!isLoggedIn) {
      console.log('🚫 SSE 미실행 - 로그인 필요', {
        isSuccess,
        hasProfile: !!profile,
        isLoading,
      });
      return;
    }

    const eventSource = subscribeNotifications();

    eventSource.onopen = event => {
      console.log('✅ [전역 SSE] 연결 성공', event);
    };

    eventSource.addEventListener('heartbeat', event => {
      console.log('💓 [전역 SSE] heartbeat 수신:', event.data);
    });

    eventSource.addEventListener('connect', event => {
      console.log('🔗 [전역 SSE] connect 이벤트 수신:', event.data);
    });

    eventSource.addEventListener('notification', event => {
      console.log('🔔 [전역 SSE] notification 수신:', event.data);

      try {
        const sseData: SSENotification = JSON.parse(event.data);

        const newNotificationResponse: SSENotification = {
          notificationType: sseData.notificationType,
          targetType: sseData.targetType,
          targetId: sseData.targetId,
          message: sseData.message,
          isRead: false,
        };

        const currentNotifications =
          queryClient.getQueryData<NotificationItem[]>(['notifications']) || [];

        const updatedNotifications = [newNotificationResponse, ...currentNotifications];

        queryClient.setQueryData(['notifications'], updatedNotifications);

        if (sseData.notificationType === 'NEW_COMMENT_ON_MOMENT') {
          showSuccess('새로운 댓글이 달렸습니다!');
        } else if (sseData.notificationType === 'NEW_REPLY_ON_COMMENT') {
          showSuccess('댓글에 답장이 달렸습니다!');
        }

        if (sseData.targetType === 'MOMENT') {
          queryClient.invalidateQueries({ queryKey: ['moments'] });
        } else if (sseData.targetType === 'COMMENT') {
          queryClient.invalidateQueries({ queryKey: ['comments'] });
        }
      } catch (error) {
        console.error('❌ [전역 SSE] 데이터 파싱 에러:', error);
        showError('실시간 알림 데이터 처리 중 오류가 발생했습니다.');
      }
    });

    eventSource.onerror = error => {
      console.error('❌ [전역 SSE] 연결 에러:', error);
    };

    return () => {
      console.log('🔌 [전역 SSE] 연결 해제...');
      eventSource.close();
    };
  }, [isLoggedIn, queryClient, showError, showSuccess]);

  return { isConnected: isLoggedIn };
};
