export const getReasonText = (reason: string) => {
  const reasonMap: Record<string, string> = {
    COMMENT_CREATION: '코멘트 작성',
    MOMENT_CREATION: '모멘트 작성',
    DAILY_LOGIN: '일일 로그인',
    PURCHASE: '구매',
    ECHO_RECEIVED: '에코 받음',
    MOMENT_ADDITIONAL_USE: '모멘트 추가 작성',
    NICKNAME_CHANGE: '닉네임 변경',
  };
  return reasonMap[reason] || reason;
};

export const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};
