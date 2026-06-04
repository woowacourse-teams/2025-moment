import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { queryKeys } from '@/shared/lib/queryKeys';
import { toast } from '@/shared/store/toast';
import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect, useRef } from 'react';
import { subscribeNotifications } from '../api/subscribeNotifications';
import { NotificationResponse } from '../types/notifications';
import {
  getNotificationInvalidationTargets,
  isSseNotificationPayload,
  mapSsePayloadToNotificationItem,
  parseSsePayload,
  prependNotificationToCache,
} from '../utils/sseNotificationPayload';

export const useSSENotifications = () => {
  const queryClient = useQueryClient();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const eventSourceRef = useRef<EventSource | null>(null);
  const isFirstConnectRef = useRef(true);

  const connect = useCallback(() => {
    if (eventSourceRef.current && eventSourceRef.current.readyState !== EventSource.CLOSED) {
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
      const parsedPayload = parseSsePayload(event.data);
      if (!parsedPayload.ok) {
        toast.error('실시간 알림 데이터 처리 중 오류가 발생했습니다.');
        return;
      }

      try {
        if (!isSseNotificationPayload(parsedPayload.value)) {
          toast.error('실시간 알림 데이터 처리 중 오류가 발생했습니다.');
          return;
        }

        const sseData = parsedPayload.value;
        const newNotification = mapSsePayloadToNotificationItem(sseData);
        const currentData = queryClient.getQueryData<NotificationResponse>(
          queryKeys.notifications.all,
        );

        queryClient.setQueryData(
          queryKeys.notifications.all,
          prependNotificationToCache(currentData, newNotification),
        );

        if (sseData.notificationType === 'NEW_COMMENT_ON_MOMENT') {
          toast.message(
            '나의 모멘트에 코멘트가 달렸습니다!',
            sseData.link ? 'moment' : undefined,
            5000,
            sseData.link ?? undefined,
          );
        }

        getNotificationInvalidationTargets(sseData).forEach(queryKey => {
          queryClient.invalidateQueries({ queryKey });
        });
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
        if (!eventSourceRef.current || eventSourceRef.current.readyState === EventSource.CLOSED) {
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
