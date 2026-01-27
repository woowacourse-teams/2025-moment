/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const commentsData = [
  {
    id: 1,
    content: '힘내세요! 응원합니다.',
    createdAt: '2025-01-20T10:00:00',
    imageUrl: null,
    moment: {
      id: 101,
      nickName: '별빛 가득한 아리아',
      content: '오늘은 정말 힘든 하루였어요.',
      imageUrl: null,
      createdAt: '2025-01-20T09:00:00',
    },
    commentNotification: {
      isRead: false,
      notificationIds: [1001],
    },
  },
  {
    id: 2,
    content: '저도 비슷한 경험이 있어요.',
    createdAt: '2025-01-20T11:00:00',
    imageUrl: null,
    moment: {
      id: 102,
      nickName: '푸른 하늘의 테리우스',
      content: '새로운 도전을 시작했어요!',
      imageUrl: null,
      createdAt: '2025-01-20T10:30:00',
    },
    commentNotification: {
      isRead: true,
      notificationIds: [],
    },
  },
] as const;

describe('나의 코멘트 모음집 페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    // 모든 코멘트 가져오기
    cy.intercept('GET', '**/api/v2/groups/1/my-comments?*', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          comments: commentsData,
          hasNextPage: false,
          nextCursor: null,
          pageSize: 10,
        },
      },
    }).as('getComments');

    // 읽지 않은 코멘트 가져오기 (필터링 여부와 상관없이 호출될 수 있음)
    cy.intercept('GET', '**/api/v2/groups/1/my-comments/unread?*', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          comments: [],
          hasNextPage: false,
          nextCursor: null,
          pageSize: 10,
        },
      },
    }).as('getUnreadComments');

    cy.visit('/groups/1/collection/my-comment');

    // 두 쿼리가 모두 완료될 때까지 대기
    cy.wait([
      '@checkLogin',
      '@getProfile',
      '@getNotifications',
      '@getComments',
      '@getUnreadComments',
    ]);
  });

  describe('시나리오 1: 코멘트 확인', () => {
    it('내가 작성한 코멘트와 관련 정보를 볼 수 있다', () => {
      cy.contains(commentsData[0].content).should('be.visible');
      cy.contains(commentsData[1].content).should('be.visible');

      cy.contains(commentsData[0].moment!.content).should('be.visible');
      cy.contains(commentsData[1].moment!.content).should('be.visible');
    });
  });
});
