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
      level: 'ASTEROID_WHITE',
      content: '오늘은 정말 힘든 하루였어요.',
      imageUrl: null,
      tagNames: ['일상/생각'],
      createdAt: '2025-01-20T09:00:00',
    },
    echos: [
      {
        id: 1,
        echoType: 'THANKS',
        userId: 1,
      },
      {
        id: 2,
        echoType: 'TOUCHED',
        userId: 2,
      },
    ],
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
      level: 'ASTEROID_WHITE',
      content: '새로운 도전을 시작했어요!',
      imageUrl: null,
      tagNames: ['일/성장'],
      createdAt: '2025-01-20T10:30:00',
    },
    echos: [],
    commentNotification: {
      isRead: true,
      notificationIds: [],
    },
  },
] as const;

describe('나의 코멘트 모음집 페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    cy.intercept('GET', '**/api/v1/comments/me?pageSize=10', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          items: commentsData,
          hasNextPage: true,
          nextCursor: 'next_page_cursor',
        },
      },
    }).as('getComments');

    cy.intercept('GET', '**/api/v1/comments/me/unread?pageSize=10', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          items: commentsData,
          hasNextPage: false,
          nextCursor: null,
        },
      },
    }).as('getUnreadComments');

    cy.visit('/collection/my-comment');
    cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getComments']);
  });

  describe('시나리오 1: 코멘트 확인', () => {
    it('내가 작성한 코멘트와 관련 정보를 볼 수 있다', () => {
      cy.contains(commentsData[0].content).should('be.visible');
      cy.contains(commentsData[1].content).should('be.visible');

      cy.contains(commentsData[0].moment?.content).should('be.visible');
      cy.contains(commentsData[1].moment?.content).should('be.visible');

      cy.contains(commentsData[0].moment?.tagNames[0]).should('be.visible');
      cy.contains(commentsData[1].moment?.tagNames[0]).should('be.visible');
    });
  });
});
