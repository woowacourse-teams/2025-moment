import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks/useToast';
import { useQueryClient } from '@tanstack/react-query';
import { useEffect } from 'react';
import { subscribeNotifications } from '../api/subscribeNotifications';
import { NotificationItem, NotificationResponse } from '../types/notifications';
import { SSENotification } from '../types/sseNotification';

const ECHO_REWARD_POINT = 3;

export const useSSENotifications = () => {
  const queryClient = useQueryClient();
  const { showError, showSuccess, showMessage } = useToast();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  useEffect(() => {
    if (!isLoggedIn) {
      console.log('🚫 SSE 미실행 - 로그인 필요');
      return;
    }

    const eventSource = subscribeNotifications();

    eventSource.onopen = event => {
      console.log('✅ [SSE] 연결 성공', event);
    };

    eventSource.addEventListener('heartbeat', event => {
      console.log('💓 [SSE] heartbeat 수신:', event.data);
    });

    eventSource.addEventListener('connect', event => {
      console.log('🔗 [SSE] connect 이벤트 수신:', event.data);
    });

    eventSource.addEventListener('notification', event => {
      console.log('🔔 [SSE] notification 수신:', event.data);

      try {
        const sseData: SSENotification = JSON.parse(event.data);

        const newNotification: NotificationItem = {
          notificationType: sseData.notificationType,
          targetType: sseData.targetType,
          targetId: sseData.targetId,
          message: sseData.message,
          isRead: false,
        };

        const currentData = queryClient.getQueryData<NotificationResponse>(['notifications']);
        const currentNotifications = currentData?.data || [];

        const updatedNotifications = [newNotification, ...currentNotifications];

        const updatedData: NotificationResponse = {
          status: 200,
          data: updatedNotifications,
        };

        queryClient.setQueryData(['notifications'], updatedData);

        if (sseData.notificationType === 'NEW_COMMENT_ON_MOMENT') {
          showMessage('나의 모멘트에 코멘트가 달렸습니다!', 'moment');
        } else if (sseData.notificationType === 'NEW_REPLY_ON_COMMENT') {
          showMessage(
            `나의 코멘트에 에코가 달렸습니다! 별조각 ${ECHO_REWARD_POINT}개를 획득했습니다!`,
            'comment',
          );
        }

        if (sseData.targetType === 'MOMENT') {
          queryClient.invalidateQueries({ queryKey: ['moments'] });
        } else if (sseData.targetType === 'COMMENT') {
          queryClient.invalidateQueries({ queryKey: ['comments'] });
        }
        queryClient.invalidateQueries({ queryKey: ['notifications'] });
      } catch (error) {
        console.error(error);
        showError('실시간 알림 데이터 처리 중 오류가 발생했습니다.');
      }
    });

    eventSource.onerror = error => {
      console.error('❌ [SSE] 연결 에러:', error);
    };

    return () => {
      console.log('🔌 [SSE] 연결 해제...');
      eventSource.close();
    };
  }, [isLoggedIn, queryClient, showError, showSuccess, showMessage]);

  return { isConnected: isLoggedIn };
};
