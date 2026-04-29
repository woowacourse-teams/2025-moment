import { truncateEmail } from './email';

describe('truncateEmail', () => {
  describe('로컬 파트가 maxLength 이하인 경우 원본 반환', () => {
    it('짧은 이메일은 그대로 반환한다', () => {
      expect(truncateEmail('short@example.com')).toBe('short@example.com');
    });

    it('로컬 파트가 정확히 15자이면 그대로 반환한다', () => {
      expect(truncateEmail('123456789012345@example.com')).toBe('123456789012345@example.com');
    });

    it('한 글자 로컬 파트도 그대로 반환한다', () => {
      expect(truncateEmail('a@b.com')).toBe('a@b.com');
    });
  });

  describe('로컬 파트가 maxLength 초과인 경우 잘라서 반환', () => {
    it('기본 maxLength(15)로 긴 이메일을 잘라낸다', () => {
      expect(truncateEmail('verylongemailaddress@apple.user')).toBe(
        'verylongemailad...@apple.user',
      );
    });

    it('잘린 로컬 파트가 정확히 15자여야 한다', () => {
      const result = truncateEmail('verylongemailaddress@apple.user');
      const localPart = result.split('@')[0].replace('...', '');
      expect(localPart.length).toBe(15);
    });

    it('maxLength를 5로 지정하면 5자까지만 표시한다', () => {
      expect(truncateEmail('verylonglocal@example.com', 5)).toBe('veryl...@example.com');
    });

    it('maxLength를 5로 지정할 때 정확히 5자가 잘린다', () => {
      expect(truncateEmail('abcdefgh@domain.com', 5)).toBe('abcde...@domain.com');
    });
  });

  describe('엣지 케이스', () => {
    it('@가 없는 문자열은 그대로 반환한다', () => {
      expect(truncateEmail('notanemail')).toBe('notanemail');
    });

    it('빈 문자열은 그대로 반환한다', () => {
      expect(truncateEmail('')).toBe('');
    });

    it('도메인 부분은 잘리지 않는다', () => {
      const email = 'verylongemailaddress@verylongdomainname.com';
      const result = truncateEmail(email);
      expect(result).toBe('verylongemailad...@verylongdomainname.com');
    });
  });
});
