export const mockProfile = {
  status: 200,
  data: {
    nickname: 'mimi',
    level: 'METEOR_WHITE',
    expStar: 100,
    nextStepExp: 200,
    availableStar: 90,
    email: 'test@gamil.com',
  },
};

export const mockRewardHistory = {
  status: 200,
  data: {
    items: [
      {
        id: 1,
        changeStart: 5,
        reason: 'COMMENT_CREATION',
        createdAt: '2025-07-21T10:57:08.926954',
      },
      {
        id: 2,
        changeStart: -5,
        reason: 'COMMENT_CREATION',
        createdAt: '2025-07-21T10:57:08.926954',
      },
    ],
  },
};
