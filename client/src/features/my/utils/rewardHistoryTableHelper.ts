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

export const getLevelText = (level: string) => {
  const levelMap: Record<string, string> = {
    ASTEROID_WHITE: '소행성 1단계',
    ASTEROID_YELLOW: '소행성 2단계',
    ASTEROID_SKY: '소행성 3단계',
    METEOR_WHITE: '유성 1단계',
    METEOR_YELLOW: '유성 2단계',
    METEOR_SKY: '유성 3단계',
    COMET_WHITE: '혜성 1단계',
    COMET_YELLOW: '혜성 2단계',
    COMET_SKY: '혜성 3단계',
    ROCKY_PLANET_WHITE: '행성 1단계',
    ROCKY_PLANET_YELLOW: '행성 2단계',
    ROCKY_PLANET_SKY: '행성 3단계',
    GAS_GIANT_WHITE: '거대 행성 1단계',
    GAS_GIANT_YELLOW: '거대 행성 2단계',
    GAS_GIANT_SKY: '거대 행성 3단계',
  };
  return levelMap[level] || level;
};

export const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  const koreaTime = new Date(date.getTime() + 9 * 60 * 60 * 1000);

  return koreaTime.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};
