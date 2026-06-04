import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { queryKeys } from '@/shared/lib/queryKeys';
import { toast } from '@/shared/store/toast';
import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect, useRef } from 'react';
import { subscribeNotifications } from '../api/subscribeNotifications';
import { NotificationResponse } from '../types/notifications';
import {
  getNotificationInvalidationTargets,
  getSseNotificationToast,
  isSseNotificationPayload,
  mapSsePayloadToNotificationItem,
  parseSsePayload,
  prependNotificationToCache,
} from '../utils/sseNotificationPayload';
import { reportSseNotificationError } from '../utils/sseNotificationError';

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
        reportSseNotificationError(parsedPayload.reason);
        return;
      }

      try {
        if (!isSseNotificationPayload(parsedPayload.value)) {
          reportSseNotificationError('invalid-payload');
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

        const notificationToast = getSseNotificationToast(sseData);
        if (notificationToast) {
          toast.message(
            notificationToast.message,
            sseData.link ? notificationToast.routeType : undefined,
            5000,
            sseData.link ?? undefined,
          );
        }

        getNotificationInvalidationTargets(sseData).forEach(queryKey => {
          queryClient.invalidateQueries({ queryKey });
        });
      } catch (error) {
        reportSseNotificationError('unknown', error);
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
