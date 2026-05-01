import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { queryKeys } from '@/shared/lib/queryKeys';
import { toast } from '@/shared/store/toast';
import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect, useRef } from 'react';
import { subscribeNotifications } from '../api/subscribeNotifications';
import { NotificationItem, NotificationResponse } from '../types/notifications';
import { SSENotification } from '../types/sseNotification';

export const useSSENotifications = () => {
  const queryClient = useQueryClient();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const eventSourceRef = useRef<EventSource | null>(null);
  const isFirstConnectRef = useRef(true);

  const connect = useCallback(() => {
    if (
      eventSourceRef.current &&
      eventSourceRef.current.readyState !== EventSource.CLOSED
    ) {
      return;
    }

    const eventSource = subscribeNotifications();
    eventSourceRef.current = eventSource;

    const handleOpen = () => {
      if (isFirstConnectRef.current) {
        isFirstConnectRef.current = false;
      } else {
        queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
      }
    };

    const handleNotification = (event: MessageEvent) => {
      try {
        const sseData: SSENotification = JSON.parse(event.data);

        const newNotification: NotificationItem = {
          notificationType: sseData.notificationType,
          targetType: sseData.targetType || 'MOMENT',
          targetId: sseData.targetId || 0,
          message: sseData.message,
          isRead: false,
        };

        const currentData = queryClient.getQueryData<NotificationResponse>(
          queryKeys.notifications.all,
        );
        const currentNotifications = currentData?.data || [];

        queryClient.setQueryData(queryKeys.notifications.all, {
          status: 200,
          data: [newNotification, ...currentNotifications],
        } satisfies NotificationResponse);

        if (sseData.notificationType === 'NEW_COMMENT_ON_MOMENT') {
          toast.message('나의 모멘트에 코멘트가 달렸습니다!', 'moment', 5000, sseData.link);
        }

        if (sseData.targetType === 'MOMENT') {
          queryClient.invalidateQueries({ queryKey: ['moments'] });
        } else if (sseData.targetType === 'COMMENT') {
          queryClient.invalidateQueries({ queryKey: ['comments'] });
        }
        queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
      } catch {
        toast.error('실시간 알림 데이터 처리 중 오류가 발생했습니다.');
      }
    };

    const handleError = () => {
      if (eventSource.readyState === EventSource.CLOSED) {
        eventSourceRef.current = null;
      }
    };

    eventSource.onopen = handleOpen;
    eventSource.onerror = handleError;
    eventSource.addEventListener('notification', handleNotification);
  }, [queryClient]);

  useEffect(() => {
    if (!isLoggedIn) return;

    connect();

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        if (
          !eventSourceRef.current ||
          eventSourceRef.current.readyState === EventSource.CLOSED
        ) {
          isFirstConnectRef.current = false;
          connect();
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      eventSourceRef.current?.close();
      eventSourceRef.current = null;
    };
  }, [isLoggedIn, connect]);

  return { isConnected: isLoggedIn };
};
