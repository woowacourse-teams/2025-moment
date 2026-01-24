export const formatRelativeTime = (dateString?: string): string => {
  if (!dateString) {
    return '';
  }
  if (dateString.includes('전') || dateString.includes('ago')) {
    return dateString;
  }

  const now = new Date();
  const targetDate = new Date(dateString);
  const targetKoreaTime = new Date(targetDate.getTime() + 9 * 60 * 60 * 1000);

  const diffInSeconds = Math.floor((now.getTime() - targetKoreaTime.getTime()) / 1000);

  if (isNaN(targetDate.getTime())) {
    throw new Error('Invalid date string provided');
  }

  if (diffInSeconds < 60) {
    return '방금 전';
  }

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return `${diffInMinutes}분 전`;
  }

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return `${diffInHours}시간 전`;
  }

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays}일 전`;
  }

  const diffInWeeks = Math.floor(diffInDays / 7);
  if (diffInWeeks < 4) {
    return `${diffInWeeks}주일 전`;
  }

  const diffInMonths =
    (now.getFullYear() - targetKoreaTime.getFullYear()) * 12 +
    (now.getMonth() - targetKoreaTime.getMonth());
  if (diffInMonths < 12) {
    return `${diffInMonths}개월 전`;
  }

  const diffInYears = now.getFullYear() - targetKoreaTime.getFullYear();
  return `${diffInYears}년 전`;
};
