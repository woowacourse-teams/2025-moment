/// <reference types="cypress" />

describe('나의 코멘트 모음집 페이지', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/api/v1/auth/login/check', {
      statusCode: 200,
      body: {
        status: 200,
        data: { isLogged: true },
      },
    }).as('checkLogin');

    cy.intercept('GET', '**/api/v1/users/me', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          nickname: '따뜻한 마음의 트리톤',
          level: 'ASTEROID_WHITE',
          expStar: 10,
          nextStepExp: 50,
        },
      },
    }).as('getProfile');

    cy.intercept('GET', '**/api/v1/notifications*', {
      statusCode: 200,
      body: {
        status: 200,
        data: [],
      },
    }).as('getNotifications');

    cy.intercept('GET', '**/api/v1/comments/me?pageSize=10', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          items: [
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
                imageUrl: 'https://example.com/image1.jpg',
                tagNames: ['일상/생각'],
                createdAt: '2025-01-20T09:00:00',
              },
              echos: [
                {
                  id: 1,
                  echoType: 'THANKS',
                },
                {
                  id: 2,
                  echoType: 'TOUCHED',
                },
              ],
              notificationId: 1001,
              read: false,
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
              notificationId: null,
              read: true,
            },
          ],
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
          items: [
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
                imageUrl: 'https://example.com/image1.jpg',
                tagNames: ['일상/생각'],
                createdAt: '2025-01-20T09:00:00',
              },
              echos: [
                {
                  id: 1,
                  echoType: 'THANKS',
                },
                {
                  id: 2,
                  echoType: 'TOUCHED',
                },
              ],
              notificationId: 1001,
              read: false,
            },
          ],
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
      cy.contains('힘내세요! 응원합니다.').should('be.visible');
      cy.contains('저도 비슷한 경험이 있어요.').should('be.visible');

      cy.contains('오늘은 정말 힘든 하루였어요.').should('be.visible');
      cy.contains('새로운 도전을 시작했어요!').should('be.visible');

      cy.contains('일상/생각').should('be.visible');
      cy.contains('일/성장').should('be.visible');

      cy.get('img[src*="example.com/image1.jpg"]').should('exist');

      cy.contains('마음 고마워요').should('be.visible');
      cy.contains('감동 받았어요').should('be.visible');
    });
  });
});
