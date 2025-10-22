/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const TEST_MOMENT = {
  content: '오늘 정말 행복한 하루였어요!',
  tagNames: ['일상/생각', '인간관계'],
} as const;

const TEST_EXTRA_MOMENT = {
  content: '추가로 더 좋은 일이 생겼어요!',
  tagNames: ['일상/생각'],
} as const;

const MOCK_MY_MOMENTS = [
  {
    id: 1,
    momenterId: 1,
    content: '첫 번째 모멘트입니다.',
    tagNames: ['태그1'],
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
    tagNames: ['태그2'],
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

      cy.intercept('GET', '**/api/v1/notifications*', {
        statusCode: 200,
        body: {
          status: 200,
          data: [],
        },
      }).as('getNotifications');

      cy.intercept('GET', '**/api/v1/moments/writable/basic', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            status: 'ALLOWED',
          },
        },
      }).as('getMomentWritingStatus');

      cy.intercept('POST', '**/api/v1/moments', req => {
        expect(req.body).to.have.property('content');
        expect(req.body).to.have.property('tagNames');

        req.reply({
          statusCode: 201,
          body: {
            status: 201,
            data: {
              id: 1,
              content: req.body.content,
              tagNames: req.body.tagNames,
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

      TEST_MOMENT.tagNames.forEach(tag => {
        cy.contains(tag).click();
      });

      cy.get('button').contains('모멘트 공유하기').should('not.be.disabled');
      cy.get('button').contains('모멘트 공유하기').click();

      cy.wait('@sendMoment').then(interception => {
        expect(interception.request.body.content).to.equal(TEST_MOMENT.content);
        expect(interception.request.body.tagNames).to.deep.equal(TEST_MOMENT.tagNames);
      });

      cy.url().should('include', '/today-moment/success');
      cy.contains('오늘의 모멘트를 공유했어요!').should('be.visible');
    });

    it('빈 내용으로는 모멘트를 작성할 수 없다', () => {
      cy.get('textarea').should('have.value', '');
      cy.get('button').contains('모멘트 공유하기').should('be.disabled');
    });

    it('성공 페이지에서 별조각이 부족하면 추가 모멘트 작성이 불가능하다', () => {
      // 먼저 모멘트 작성
      cy.get('textarea').clear().type(TEST_MOMENT.content);
      TEST_MOMENT.tagNames.forEach(tag => {
        cy.contains(tag).click();
      });
      cy.get('button').contains('모멘트 공유하기').click();
      cy.wait('@sendMoment');

      // 성공 페이지로 이동
      cy.url().should('include', '/today-moment/success');

      // extraWritable API intercept (별조각 부족)
      cy.intercept('GET', '**/api/v1/moments/writable/extra', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            status: 'DENIED',
          },
        },
      }).as('getMomentExtraWritableDenied');

      // 추가 작성하기 버튼 클릭
      cy.contains('추가 작성하기').click();

      cy.wait('@getMomentExtraWritableDenied');

      // 모달 확인
      cy.get('[role="dialog"]').should('be.visible');
      cy.contains('추가 작성하려면 별조각 10개가 필요합니다.').should('be.visible');
      cy.contains('별조각을 모아오세요.').should('be.visible');

      // "추가 작성하기" 버튼이 없어야 함 (별조각 부족)
      cy.get('[role="dialog"]').find('button').contains('추가 작성하기').should('not.exist');

      // 닫기 버튼만 있어야 함
      cy.get('[role="dialog"]').find('button').contains('닫기').should('be.visible');
    });
  });

  describe('시나리오 2: 추가 모멘트 작성', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('POST', '**/api/v1/moments/extra', req => {
        expect(req.body).to.have.property('content');
        expect(req.body).to.have.property('tagNames');

        req.reply({
          statusCode: 201,
          body: {
            status: 201,
            data: {
              id: 2,
              content: req.body.content,
              tagNames: req.body.tagNames,
            },
          },
        });
      }).as('sendExtraMoment');

      cy.visit('/today-moment-extra');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getNotifications');
    });

    it('추가 모멘트가 작성 가능한 경우 추가 모멘트를 작성할 수 있다', () => {
      cy.contains('모멘트 공유하기').should('be.visible');

      cy.get('textarea').should('be.visible');
      cy.get('textarea').clear().type(TEST_EXTRA_MOMENT.content);

      TEST_EXTRA_MOMENT.tagNames.forEach(tag => {
        cy.contains(tag).click();
      });

      cy.get('button').contains('모멘트 공유하기').should('not.be.disabled');
      cy.get('button').contains('모멘트 공유하기').click();

      cy.wait('@sendExtraMoment').then(interception => {
        expect(interception.request.body.content).to.equal(TEST_EXTRA_MOMENT.content);
        expect(interception.request.body.tagNames).to.deep.equal(TEST_EXTRA_MOMENT.tagNames);
      });

      cy.url().should('include', '/today-moment/success');
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
        tagNames: ['태그1'],
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
