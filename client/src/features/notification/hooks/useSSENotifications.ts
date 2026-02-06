import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { toasts } from '@/shared/store/toast';
import { useQueryClient } from '@tanstack/react-query';
import { useEffect, useRef } from 'react';
import { subscribeNotifications } from '../api/subscribeNotifications';
import { NotificationItem, NotificationResponse } from '../types/notifications';
import { SSENotification } from '../types/sseNotification';

export const useSSENotifications = () => {
  const queryClient = useQueryClient();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (!isLoggedIn) {
      return;
    }

    if (eventSourceRef.current) {
      return;
    }

    const eventSource = subscribeNotifications();
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {};

    eventSource.addEventListener('heartbeat', () => {});

    eventSource.addEventListener('connect', () => {});

    eventSource.addEventListener('notification', event => {
      try {
        const sseData: SSENotification = JSON.parse(event.data);

        const newNotification: NotificationItem = {
          notificationType: sseData.notificationType,
          targetType: sseData.targetType || 'MOMENT',
          targetId: sseData.targetId || 0,
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
          toasts.message('나의 모멘트에 코멘트가 달렸습니다!', 'moment', 5000, sseData.link);
        }

        if (sseData.targetType === 'MOMENT') {
          queryClient.invalidateQueries({ queryKey: ['moments'] });
        } else if (sseData.targetType === 'COMMENT') {
          queryClient.invalidateQueries({ queryKey: ['comments'] });
        }
        queryClient.invalidateQueries({ queryKey: ['notifications'] });
      } catch {
        toasts.error('실시간 알림 데이터 처리 중 오류가 발생했습니다.');
      }
    });

    eventSource.onerror = () => {};

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, [isLoggedIn, queryClient]);

  return { isConnected: isLoggedIn };
};
