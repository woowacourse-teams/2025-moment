/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const TEST_MOMENT = {
  content: '오늘 정말 행복한 하루였어요!',
} as const;

const MOCK_MY_MOMENTS = [
  {
    id: 1,
    momenterId: 1,
    content: '첫 번째 모멘트입니다.',
    imageUrl: null,
    createdAt: '2025-10-22T10:00:00',
    comments: [
      {
        id: 1,
        content: '댓글 내용입니다.',
        nickname: '댓글작성자',
        level: 'ASTEROID_WHITE',
        createdAt: '2025-10-22T10:30:00',
        imageUrl: null,
        echos: [],
      },
    ],
    momentNotification: {
      isRead: true,
      notificationIds: [],
    },
  },
  {
    id: 2,
    momenterId: 1,
    content: '두 번째 모멘트입니다.',
    imageUrl: null,
    createdAt: '2025-10-22T11:00:00',
    comments: null,
    momentNotification: {
      isRead: true,
      notificationIds: [],
    },
  },
] as const;

describe('오늘의 모멘트 페이지', () => {
  describe('시나리오 1: 모멘트 작성 및 성공 페이지', () => {
    beforeEach(() => {
      cy.intercept('GET', '**/api/v1/notifications/subscribe', {
        statusCode: 200,
        headers: {
          'content-type': 'text/event-stream',
        },
        body: '',
      }).as('notificationSubscribe');

      // 인증 상태를 계속 유지하도록 설정
      cy.intercept('GET', '**/api/v2/auth/login/check', {
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
            nickname: '테스트유저',
            level: 1,
            expStar: 50,
            nextStepExp: 100,
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

      cy.intercept('POST', '**/api/v1/moments', req => {
        expect(req.body).to.have.property('content');

        req.reply({
          statusCode: 201,
          body: {
            status: 201,
            data: {
              id: 1,
              content: req.body.content,
              imageUrl: req.body.imageUrl || null,
              imageName: req.body.imageName || null,
            },
          },
        });
      }).as('sendMoment');

      cy.visit('/today-moment');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getMomentWritingStatus');
      cy.wait('@getNotifications');
    });

    it('텍스트를 입력하고 모멘트를 작성하면 성공 페이지로 이동한다', () => {
      cy.contains('오늘의 모멘트').should('be.visible');
      cy.contains('하루에 한 번, 당신의 특별한 모멘트를 공유해보세요').should('be.visible');

      cy.get('textarea').should('be.visible');
      cy.get('textarea').clear().type(TEST_MOMENT.content);

      cy.get('button').contains('모멘트 공유하기').should('not.be.disabled');
      cy.get('button').contains('모멘트 공유하기').click();

      cy.wait('@sendMoment').then(interception => {
        expect(interception.request.body.content).to.equal(TEST_MOMENT.content);
      });

      cy.url().should('include', '/today-moment/success');
      cy.contains('오늘의 모멘트를 공유했어요!').should('be.visible');
    });

    it('빈 내용으로는 모멘트를 작성할 수 없다', () => {
      cy.get('textarea').should('have.value', '');
      cy.get('button').contains('모멘트 공유하기').should('be.disabled');
    });
  });

  describe('시나리오 3: 모멘트 조회 (나의 모멘트 컬렉션)', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('GET', '**/api/v1/moments/me*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            items: MOCK_MY_MOMENTS,
            nextCursor: null,
            hasNextPage: false,
            pageSize: 10,
          },
        },
      }).as('getMyMoments');

      cy.visit('/collection/my-moment');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getMyMoments');
      cy.wait('@getNotifications');
    });

    it('작성한 모멘트 목록을 확인할 수 있다', () => {
      cy.contains('나의 모멘트').should('be.visible');

      MOCK_MY_MOMENTS.forEach(moment => {
        cy.contains(moment.content).should('be.visible');
      });
    });
  });

  describe('시나리오 4: 코멘트 신고하기', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('GET', '**/api/v1/moments/me*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            items: MOCK_MY_MOMENTS,
            nextCursor: null,
            hasNextPage: false,
            pageSize: 10,
          },
        },
      }).as('getMyMoments');

      cy.intercept('POST', '**/api/v1/comments/*/reports', req => {
        expect(req.body).to.have.property('reason');

        req.reply({
          statusCode: 201,
          body: {
            status: 201,
            data: {
              id: 1,
              reason: req.body.reason,
            },
          },
        });
      }).as('reportComment');

      cy.visit('/collection/my-moment');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getMyMoments');
      cy.wait('@getNotifications');
    });

    it('코멘트를 신고할 수 있다', () => {
      const firstMoment = MOCK_MY_MOMENTS[0];
      cy.contains(firstMoment.content).should('be.visible');

      // 모멘트 카드를 클릭해서 코멘트 모달 열기
      cy.contains(firstMoment.content).click();

      // 모달이 열릴 때까지 대기
      cy.get('[role="dialog"]').should('be.visible');
      cy.contains('댓글 내용입니다.').should('be.visible');

      // 신고 버튼 클릭
      cy.get('button[class*="ComplaintButton"]').first().click();

      cy.contains('신고하기').should('be.visible');
      cy.contains('신고 사유를 선택해주세요').should('be.visible');

      // 신고 사유 선택 및 제출
      cy.contains('스팸/광고').click();
      cy.get('button').contains('신고하기').last().click();

      cy.wait('@reportComment');

      cy.contains('신고가 접수되었습니다').should('be.visible');
    });
  });

  describe('시나리오 5: 모멘트 알림 확인', () => {
    beforeEach(() => {
      // 인증 관련 intercept를 먼저 설정
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
            nickname: '테스트유저',
            level: 1,
            expStar: 50,
            nextStepExp: 100,
          },
        },
      }).as('getProfile');

      cy.intercept('GET', '**/api/v1/notifications/subscribe', {
        statusCode: 200,
        headers: {
          'content-type': 'text/event-stream',
        },
        body: '',
      }).as('notificationSubscribe');

      // 알림 데이터 설정
      const mockNotifications = [
        {
          id: 1,
          notificationType: 'NEW_COMMENT_ON_MOMENT',
          targetType: 'MOMENT',
          targetId: 1,
          message: '누군가가 당신의 모멘트에 댓글을 남겼습니다.',
          isRead: false,
        },
      ];

      cy.intercept('GET', '**/api/v1/notifications*', {
        statusCode: 200,
        body: {
          status: 200,
          data: mockNotifications,
        },
      }).as('getNotificationsWithData');

      // 알림이 있는 모멘트 데이터
      const momentWithNotification = {
        id: 1,
        momenterId: 1,
        content: '댓글이 달린 모멘트입니다.',
        imageUrl: null,
        createdAt: '2025-10-22T10:00:00',
        comments: null,
        momentNotification: {
          isRead: false,
          notificationIds: [1],
        },
      };

      cy.intercept('GET', '**/api/v1/moments/me*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            items: [momentWithNotification],
            nextCursor: null,
            hasNextPage: false,
            pageSize: 10,
          },
        },
      }).as('getMyMomentsWithNotification');

      cy.visit('/collection/my-moment');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getNotificationsWithData');
      cy.wait('@getMyMomentsWithNotification');
    });

    it('모멘트 관련 알림을 확인할 수 있다', () => {
      cy.contains('나의 모멘트').should('be.visible');
      cy.contains('댓글이 달린 모멘트입니다.').should('be.visible');
    });
  });
});
