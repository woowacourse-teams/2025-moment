import * as Sentry from '@sentry/react';

export type SseNotificationErrorReason = 'parse-error' | 'invalid-payload' | 'unknown';

const normalizeError = (error: unknown): Error => {
  if (error instanceof Error) return error;

  return new Error('SSE notification processing failed');
};

/**
 * SSE 알림 데이터 처리 실패를 개발자 관측성 도구로 보고한다.
 *
 * @param reason - SSE 알림 데이터 처리 실패 원인
 * @param error - 후처리 중 발생한 원본 에러. 없으면 기본 Error를 생성한다.
 */
export const reportSseNotificationError = (
  reason: SseNotificationErrorReason,
  error?: unknown,
) => {
  Sentry.captureException(normalizeError(error), {
    level: 'warning',
    tags: {
      domain: 'notification',
      source: 'sse',
      reason,
    },
  });
};
