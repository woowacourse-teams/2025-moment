/**
 * 긴 이메일 주소를 짧게 표시합니다.
 * 특히 애플 로그인의 경우 매우 긴 이메일이 발생할 수 있어,
 * 앞부분은 최대 10자까지만 표시하고 나머지는 ...로 처리합니다.
 *
 * @param email - 표시할 이메일 주소
 * @param maxLength - 이메일 로컬 파트의 최대 길이 (기본값: 10)
 * @returns 잘린 이메일 주소
 *
 * @example
 * truncateEmail('verylongemailaddress@apple.user')
 * // returns: 'verylongem...@apple.user'
 *
 * @example
 * truncateEmail('short@example.com')
 * // returns: 'short@example.com'
 */
export function truncateEmail(email: string, maxLength: number = 15): string {
  if (!email || !email.includes('@')) {
    return email;
  }

  const [localPart, domain] = email.split('@');

  if (localPart.length <= maxLength) {
    return email;
  }

  return `${localPart.substring(0, maxLength)}...@${domain}`;
}
