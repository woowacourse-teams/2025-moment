import { formatRelativeTime } from './formatRelativeTime';

const FIXED_NOW = new Date('2026-04-28T12:00:00Z');

// dateString을 UTC로 만들되, +9h offset 후 now와의 diff가 targetDiffSeconds가 되도록 역산
const makeDateString = (diffSeconds: number): string => {
  const targetKoreaTime = new Date(FIXED_NOW.getTime() - diffSeconds * 1000);
  const targetDate = new Date(targetKoreaTime.getTime() - 9 * 60 * 60 * 1000);
  return targetDate.toISOString();
};

describe('formatRelativeTime', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(FIXED_NOW);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('엣지 케이스', () => {
    it('dateString이 undefined이면 빈 문자열을 반환한다', () => {
      expect(formatRelativeTime(undefined)).toBe('');
    });

    it('dateString이 빈 문자열이면 빈 문자열을 반환한다', () => {
      expect(formatRelativeTime('')).toBe('');
    });

    it('"전"이 포함된 문자열은 그대로 반환한다', () => {
      expect(formatRelativeTime('5분 전')).toBe('5분 전');
    });

    it('"ago"가 포함된 문자열은 그대로 반환한다', () => {
      expect(formatRelativeTime('5 minutes ago')).toBe('5 minutes ago');
    });

    it('유효하지 않은 날짜 문자열은 에러를 던진다', () => {
      expect(() => formatRelativeTime('not-a-date')).toThrow('Invalid date string provided');
    });
  });

  describe('시간 단위별 반환값', () => {
    it('30초 전이면 "방금 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(30))).toBe('방금 전');
    });

    it('59초 전이면 "방금 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(59))).toBe('방금 전');
    });

    it('1분 전이면 "1분 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(60))).toBe('1분 전');
    });

    it('5분 전이면 "5분 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(5 * 60))).toBe('5분 전');
    });

    it('59분 전이면 "59분 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(59 * 60))).toBe('59분 전');
    });

    it('1시간 전이면 "1시간 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(60 * 60))).toBe('1시간 전');
    });

    it('5시간 전이면 "5시간 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(5 * 60 * 60))).toBe('5시간 전');
    });

    it('23시간 전이면 "23시간 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(23 * 60 * 60))).toBe('23시간 전');
    });

    it('1일 전이면 "1일 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(24 * 60 * 60))).toBe('1일 전');
    });

    it('3일 전이면 "3일 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(3 * 24 * 60 * 60))).toBe('3일 전');
    });

    it('6일 전이면 "6일 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(6 * 24 * 60 * 60))).toBe('6일 전');
    });

    it('7일 전이면 "1주일 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(7 * 24 * 60 * 60))).toBe('1주일 전');
    });

    it('14일 전이면 "2주일 전"을 반환한다', () => {
      expect(formatRelativeTime(makeDateString(14 * 24 * 60 * 60))).toBe('2주일 전');
    });
  });

  describe('월/년 단위 반환값', () => {
    it('2개월 전이면 "2개월 전"을 반환한다', () => {
      // now = 2026-04-28, targetKoreaTime = 2026-02-28
      const targetDate = new Date('2026-02-28T03:00:00Z');
      expect(formatRelativeTime(targetDate.toISOString())).toBe('2개월 전');
    });

    it('1년 전이면 "1년 전"을 반환한다', () => {
      const targetDate = new Date('2025-04-28T03:00:00Z');
      expect(formatRelativeTime(targetDate.toISOString())).toBe('1년 전');
    });

    it('3년 전이면 "3년 전"을 반환한다', () => {
      const targetDate = new Date('2023-04-28T03:00:00Z');
      expect(formatRelativeTime(targetDate.toISOString())).toBe('3년 전');
    });
  });
});
