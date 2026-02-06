export const queryKeys = {
  auth: {
    checkLogin: ['checkIfLoggedIn'] as const,
    profile: ['profile'] as const,
    randomNickname: ['randomNickname'] as const,
  },
  my: {
    profile: ['my', 'profile'] as const,
  },
  groups: {
    all: ['groups'] as const,
  },
  group: {
    detail: (groupId: number) => ['group', groupId] as const,
    members: (groupId: number) => ['group', groupId, 'members'] as const,
    pending: (groupId: number) => ['group', groupId, 'pending'] as const,
    moments: (groupId: number) => ['group', groupId, 'moments'] as const,
    momentsUnread: (groupId: number) => ['group', groupId, 'moments', 'unread'] as const,
    myMoments: (groupId: number) => ['group', groupId, 'my-moments'] as const,
    moment: (groupId: number, momentId: number) => ['group', groupId, 'moment', momentId] as const,
    momentComments: (groupId: number, momentId: number) =>
      ['group', groupId, 'moment', momentId, 'comments'] as const,
    comments: (groupId: number) => ['group', groupId, 'comments'] as const,
    commentsUnread: (groupId: number) => ['group', groupId, 'comments', 'unread'] as const,
  },
  commentableMoments: {
    all: ['commentableMoments'] as const,
    byGroup: (groupId: number) => ['commentableMoments', groupId] as const,
  },
  notifications: {
    all: ['notifications'] as const,
  },
  momentWritingStatus: ['momentWritingStatus'] as const,
  rewardHistory: ['rewardHistory'] as const,
} as const;
