import type { QueryKey } from '@tanstack/react-query';
import { queryKeys } from '@/shared/lib/queryKeys';
import { NotificationItem, NotificationResponse, NotificationType } from '../types/notifications';
import { SSENotification } from '../types/sseNotification';

type ParseSsePayloadResult =
  | {
      ok: true;
      value: unknown;
    }
  | {
      ok: false;
      reason: 'parse-error';
    };

export type NotificationInvalidationTarget = QueryKey;

export const NOTIFICATION_TYPES = [
  'NEW_COMMENT_ON_MOMENT',
  'GROUP_JOIN_REQUEST',
  'GROUP_JOIN_APPROVED',
  'GROUP_KICKED',
  'MOMENT_LIKED',
  'COMMENT_LIKED',
] as const satisfies readonly NotificationType[];

/**
 * SSE notification 이벤트 문자열을 JSON payload로 파싱한다.
 *
 * @param raw - EventSource notification 이벤트의 event.data 문자열
 * @returns 파싱 성공 시 payload, 실패 시 parse-error 결과
 */
export const parseSsePayload = (raw: string): ParseSsePayloadResult => {
  try {
    return { ok: true, value: JSON.parse(raw) };
  } catch {
    return { ok: false, reason: 'parse-error' };
  }
};

/**
 * 현재 서버 NotificationSseResponse 계약을 런타임에서 확인한다.
 *
 * @param payload - JSON 파싱 이후 아직 신뢰할 수 없는 SSE payload
 * @returns 클라이언트가 처리 가능한 SSE 알림 payload인지 여부
 */
export const isSseNotificationPayload = (payload: unknown): payload is SSENotification => {
  if (!payload || typeof payload !== 'object') return false;

  const candidate = payload as Record<string, unknown>;

  return (
    typeof candidate.notificationId === 'number' &&
    NOTIFICATION_TYPES.includes(candidate.notificationType as NotificationType) &&
    typeof candidate.message === 'string' &&
    (typeof candidate.link === 'string' || candidate.link === null)
  );
};

/**
 * 서버 SSE DTO를 알림 목록 cache가 사용하는 내부 item shape으로 변환한다.
 *
 * @param payload - 현재 서버 NotificationSseResponse 형태의 SSE payload
 * @returns notifications.all cache에 넣을 수 있는 NotificationItem
 */
export const mapSsePayloadToNotificationItem = (payload: SSENotification): NotificationItem => ({
  id: payload.notificationId,
  notificationType: payload.notificationType,
  message: payload.message,
  isRead: false,
  link: payload.link,
});

/**
 * group-scoped query invalidation에 사용할 groupId를 link에서 추출한다.
 *
 * @param link - 서버가 내려준 딥링크. 이동 대상이 없으면 null일 수 있다.
 * @returns group 경로에서 추출한 groupId, 추출할 수 없으면 null
 */
export const getGroupIdFromLink = (link: string | null): number | null => {
  if (link === null) return null;

  const match = link.match(/\/groups\/(\d+)/);
  return match ? Number(match[1]) : null;
};

/**
 * 정상 처리된 SSE 알림 이후 stale 처리할 React Query query key 목록을 계산한다.
 *
 * 반환값은 invalidateQueries에 각각 넘길 query key들의 목록이다.
 * 기본으로 notifications.all을 포함하고, groupId와 알림 타입이 확인되면 관련 group cache key를 추가한다.
 *
 * @param payload - 현재 서버 NotificationSseResponse 형태의 SSE payload
 * @returns invalidateQueries에 사용할 React Query query key 목록
 */
export const getNotificationInvalidationTargets = (
  payload: SSENotification,
): NotificationInvalidationTarget[] => {
  const queryKeysToInvalidate: NotificationInvalidationTarget[] = [queryKeys.notifications.all];
  const groupId = getGroupIdFromLink(payload.link);

  if (!groupId) return queryKeysToInvalidate;

  if (payload.notificationType === 'NEW_COMMENT_ON_MOMENT') {
    return [
      ...queryKeysToInvalidate,
      queryKeys.group.myMoments(groupId),
      queryKeys.group.momentsUnread(groupId),
    ];
  }

  if (payload.notificationType === 'COMMENT_LIKED') {
    return [
      ...queryKeysToInvalidate,
      queryKeys.group.comments(groupId),
      queryKeys.group.commentsUnread(groupId),
    ];
  }

  return queryKeysToInvalidate;
};

/**
 * 유효한 SSE 알림 item을 notifications.all cache 앞에 추가한다.
 *
 * @param currentData - 현재 React Query notifications.all cache 값
 * @param newNotification - SSE payload에서 변환된 클라이언트 알림 item
 * @returns 새 알림이 앞에 추가된 다음 notifications.all cache 값
 */
export const prependNotificationToCache = (
  currentData: NotificationResponse | undefined,
  newNotification: NotificationItem,
): NotificationResponse => ({
  status: currentData?.status ?? 200,
  data: [newNotification, ...(currentData?.data ?? [])],
});
