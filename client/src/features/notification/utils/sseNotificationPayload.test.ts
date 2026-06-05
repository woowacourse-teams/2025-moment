import {
  getGroupIdFromLink,
  getNotificationInvalidationTargets,
  getSseNotificationToast,
  mapSsePayloadToNotificationItem,
  parseSsePayload,
  prependNotificationToCache,
} from './sseNotificationPayload';
import { queryKeys } from '@/shared/lib/queryKeys';
import { NotificationItem, NotificationResponse } from '../types/notifications';
import { SSENotification } from '../types/sseNotification';

describe('SSE 알림 payload 파싱', () => {
  it('JSON 형식이 아닌 SSE 이벤트 데이터는 parse-error로 분류한다', () => {
    const result = parseSsePayload('{invalid-json');

    expect(result).toEqual({ ok: false, reason: 'parse-error' });
  });
});

describe('SSE payload를 알림 목록 cache item으로 변환', () => {
  it('현재 서버 SSE payload를 내부 알림 item 형태로 변환한다', () => {
    const payload: SSENotification = {
      notificationId: 10,
      notificationType: 'COMMENT_LIKED',
      message: '코멘트에 좋아요가 달렸습니다.',
      link: '/groups/3/collection/my-comment',
    };

    const result = mapSsePayloadToNotificationItem(payload);

    expect(result).toEqual({
      id: 10,
      notificationType: 'COMMENT_LIKED',
      message: '코멘트에 좋아요가 달렸습니다.',
      isRead: false,
      link: '/groups/3/collection/my-comment',
    });
  });

  it('link가 null인 SSE payload는 내부 알림 item에도 null link로 유지한다', () => {
    const payload: SSENotification = {
      notificationId: 11,
      notificationType: 'GROUP_KICKED',
      message: '그룹에서 강퇴되었습니다.',
      link: null,
    };

    const result = mapSsePayloadToNotificationItem(payload);

    expect(result).toEqual({
      id: 11,
      notificationType: 'GROUP_KICKED',
      message: '그룹에서 강퇴되었습니다.',
      isRead: false,
      link: null,
    });
  });
});

describe('SSE 알림 link에서 groupId 추출', () => {
  it('link가 null이면 groupId를 추출하지 않는다', () => {
    const result = getGroupIdFromLink(null);

    expect(result).toBeNull();
  });

  it('group link에서 groupId를 추출한다', () => {
    const result = getGroupIdFromLink('/groups/3/collection/my-moment');

    expect(result).toBe(3);
  });

  it('group 경로가 아닌 link에서는 groupId를 추출하지 않는다', () => {
    const result = getGroupIdFromLink('/moments/10');

    expect(result).toBeNull();
  });
});

describe('SSE 알림 item cache prepend', () => {
  const newNotification: NotificationItem = {
    id: 10,
    notificationType: 'NEW_COMMENT_ON_MOMENT',
    message: '내 모멘트에 새로운 코멘트가 달렸습니다.',
    isRead: false,
    link: '/groups/3/collection/my-moment',
  };

  it('notifications.all cache가 없으면 새 알림 1개를 담은 cache 응답을 만든다', () => {
    const result = prependNotificationToCache(undefined, newNotification);

    expect(result).toEqual({
      status: 200,
      data: [newNotification],
    });
  });

  it('기존 알림 목록 앞에 새 알림을 추가한다', () => {
    const existingNotification: NotificationItem = {
      id: 9,
      notificationType: 'MOMENT_LIKED',
      message: '모멘트에 좋아요가 달렸습니다.',
      isRead: false,
      link: '/groups/3/collection/my-moment',
    };
    const currentData: NotificationResponse = {
      status: 200,
      data: [existingNotification],
    };

    const result = prependNotificationToCache(currentData, newNotification);

    expect(result.data).toEqual([newNotification, existingNotification]);
  });

  it('기존 알림 배열을 직접 변경하지 않는다', () => {
    const existingNotification: NotificationItem = {
      id: 9,
      notificationType: 'MOMENT_LIKED',
      message: '모멘트에 좋아요가 달렸습니다.',
      isRead: false,
      link: '/groups/3/collection/my-moment',
    };
    const currentNotifications = [existingNotification];
    const currentData: NotificationResponse = {
      status: 200,
      data: currentNotifications,
    };

    prependNotificationToCache(currentData, newNotification);

    expect(currentNotifications).toEqual([existingNotification]);
  });
});

describe('SSE 알림 invalidate target 계산', () => {
  it('정상 SSE 알림은 알림 목록 갱신 대상으로 계산한다', () => {
    const payload: SSENotification = {
      notificationId: 10,
      notificationType: 'GROUP_KICKED',
      message: '그룹에서 강퇴되었습니다.',
      link: null,
    };

    const result = getNotificationInvalidationTargets(payload);

    expect(result).toEqual([queryKeys.notifications.all]);
  });

  it('NEW_COMMENT_ON_MOMENT 알림은 모멘트 모음집 갱신 대상으로 계산한다', () => {
    const payload: SSENotification = {
      notificationId: 10,
      notificationType: 'NEW_COMMENT_ON_MOMENT',
      message: '내 모멘트에 새로운 코멘트가 달렸습니다.',
      link: '/groups/3/collection/my-moment',
    };

    const result = getNotificationInvalidationTargets(payload);

    expect(result).toEqual([
      queryKeys.notifications.all,
      queryKeys.group.myMoments(3),
      queryKeys.group.momentsUnread(3),
    ]);
  });

  it('MOMENT_LIKED 알림은 모멘트 모음집 갱신 대상으로 계산한다', () => {
    const payload: SSENotification = {
      notificationId: 13,
      notificationType: 'MOMENT_LIKED',
      message: '모멘트에 좋아요가 달렸습니다.',
      link: '/groups/3/collection/my-moment',
    };

    const result = getNotificationInvalidationTargets(payload);

    expect(result).toEqual([
      queryKeys.notifications.all,
      queryKeys.group.myMoments(3),
      queryKeys.group.momentsUnread(3),
    ]);
  });

  it('COMMENT_LIKED 알림은 코멘트 모음집 갱신 대상으로 계산한다', () => {
    const payload: SSENotification = {
      notificationId: 11,
      notificationType: 'COMMENT_LIKED',
      message: '코멘트에 좋아요가 달렸습니다.',
      link: '/groups/3/collection/my-comment',
    };

    const result = getNotificationInvalidationTargets(payload);

    expect(result).toEqual([
      queryKeys.notifications.all,
      queryKeys.group.comments(3),
      queryKeys.group.commentsUnread(3),
    ]);
  });

  it('groupId를 추출할 수 없으면 그룹 화면 갱신 대상으로 계산하지 않는다', () => {
    const payload: SSENotification = {
      notificationId: 12,
      notificationType: 'NEW_COMMENT_ON_MOMENT',
      message: '내 모멘트에 새로운 코멘트가 달렸습니다.',
      link: null,
    };

    const result = getNotificationInvalidationTargets(payload);

    expect(result).toEqual([queryKeys.notifications.all]);
  });
});

describe('SSE 알림 toast 표시 정책', () => {
  it('NEW_COMMENT_ON_MOMENT 알림은 모멘트 toast 대상이다', () => {
    const payload: SSENotification = {
      notificationId: 20,
      notificationType: 'NEW_COMMENT_ON_MOMENT',
      message: '내 모멘트에 새로운 코멘트가 달렸습니다.',
      link: '/groups/3/collection/my-moment',
    };

    const result = getSseNotificationToast(payload);

    expect(result).toEqual({
      message: '나의 모멘트에 코멘트가 달렸습니다!',
      routeType: 'moment',
    });
  });

  it('MOMENT_LIKED 알림은 모멘트 toast 대상이다', () => {
    const payload: SSENotification = {
      notificationId: 21,
      notificationType: 'MOMENT_LIKED',
      message: '모멘트에 좋아요가 달렸습니다.',
      link: '/groups/3/collection/my-moment',
    };

    const result = getSseNotificationToast(payload);

    expect(result).toEqual({
      message: '나의 모멘트에 좋아요가 달렸습니다!',
      routeType: 'moment',
    });
  });

  it('COMMENT_LIKED 알림은 코멘트 toast 대상이다', () => {
    const payload: SSENotification = {
      notificationId: 22,
      notificationType: 'COMMENT_LIKED',
      message: '코멘트에 좋아요가 달렸습니다.',
      link: '/groups/3/collection/my-comment',
    };

    const result = getSseNotificationToast(payload);

    expect(result).toEqual({
      message: '나의 코멘트에 좋아요가 달렸습니다!',
      routeType: 'comment',
    });
  });

  it.each([
    ['GROUP_JOIN_REQUEST', '그룹 가입 요청이 왔습니다.'],
    ['GROUP_JOIN_APPROVED', '그룹 가입이 승인되었습니다.'],
    ['GROUP_KICKED', '그룹에서 강퇴되었습니다.'],
  ] as const)('%s 알림은 이동 없는 toast 대상이다', (notificationType, message) => {
    const payload: SSENotification = {
      notificationId: 23,
      notificationType,
      message,
      link: notificationType === 'GROUP_KICKED' ? null : '/groups/3/today-moment',
    };

    const result = getSseNotificationToast(payload);

    expect(result).toEqual({ message });
  });
});
