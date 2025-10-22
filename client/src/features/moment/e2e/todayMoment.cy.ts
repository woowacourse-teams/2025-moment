/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const TEST_MOMENT = {
  content: '오늘 정말 행복한 하루였어요!',
  tagNames: ['행복', '일상'],
} as const;

const TEST_EXTRA_MOMENT = {
  content: '추가로 더 좋은 일이 생겼어요!',
  tagNames: ['기쁨'],
} as const;

const MOCK_MY_MOMENTS = [
  {
    id: 1,
    content: '첫 번째 모멘트입니다.',
    tagNames: ['태그1'],
    imageUrl: null,
    imageName: null,
    createdAt: '2025-10-22T10:00:00',
  },
  {
    id: 2,
    content: '두 번째 모멘트입니다.',
    tagNames: ['태그2'],
    imageUrl: null,
    imageName: null,
    createdAt: '2025-10-22T11:00:00',
  },
] as const;

describe('오늘의 모멘트 페이지', () => {
  describe('시나리오 1: 모멘트 작성 및 성공 페이지', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('GET', '**/api/v1/moments/writable/status', {
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

      cy.get('button').contains('작성 완료').should('not.be.disabled');
      cy.get('button').contains('작성 완료').click();

      cy.wait('@sendMoment').then(interception => {
        expect(interception.request.body.content).to.equal(TEST_MOMENT.content);
        expect(interception.request.body.tagNames).to.deep.equal(TEST_MOMENT.tagNames);
      });

      cy.url().should('include', '/today-moment-success');
      cy.contains('모멘트 작성 완료').should('be.visible');
    });

    it('빈 내용으로는 모멘트를 작성할 수 없다', () => {
      cy.get('textarea').should('have.value', '');
      cy.get('button').contains('작성 완료').should('be.disabled');
    });
  });

  describe('시나리오 2: 추가 모멘트 작성', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('GET', '**/api/v1/moments/writable/status', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            status: 'ALLOWED',
          },
        },
      }).as('getMomentWritingStatus');

      cy.intercept('GET', '**/api/v1/moments/writable/extra', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            extraWritable: true,
          },
        },
      }).as('getMomentExtraWritable');

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

      cy.visit('/today-moment/extra');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getMomentExtraWritable');
      cy.wait('@getNotifications');
    });

    it('추가 모멘트가 작성 가능한 경우 추가 모멘트를 작성할 수 있다', () => {
      cy.contains('추가 모멘트').should('be.visible');

      cy.get('textarea').should('be.visible');
      cy.get('textarea').clear().type(TEST_EXTRA_MOMENT.content);

      TEST_EXTRA_MOMENT.tagNames.forEach(tag => {
        cy.contains(tag).click();
      });

      cy.get('button').contains('작성 완료').should('not.be.disabled');
      cy.get('button').contains('작성 완료').click();

      cy.wait('@sendExtraMoment').then(interception => {
        expect(interception.request.body.content).to.equal(TEST_EXTRA_MOMENT.content);
        expect(interception.request.body.tagNames).to.deep.equal(TEST_EXTRA_MOMENT.tagNames);
      });

      cy.url().should('include', '/today-moment-success');
    });

    it('추가 모멘트가 작성 불가능한 경우 작성 페이지 접근이 제한된다', () => {
      cy.intercept('GET', '**/api/v1/moments/writable/extra', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            extraWritable: false,
          },
        },
      }).as('getMomentExtraWritableDisabled');

      cy.visit('/today-moment/extra');

      cy.wait('@getMomentExtraWritableDisabled');

      cy.url().should('not.include', '/today-moment/extra');
    });
  });

  describe('시나리오 3: 모멘트 조회 (나의 모멘트 컬렉션)', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('GET', '**/api/v1/users/me/moments*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            moments: MOCK_MY_MOMENTS,
            hasNext: false,
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

  describe('시나리오 4: 모멘트 신고하기', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      cy.intercept('GET', '**/api/v1/users/me/moments*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            moments: MOCK_MY_MOMENTS,
            hasNext: false,
          },
        },
      }).as('getMyMoments');

      cy.intercept('POST', '**/api/v1/moments/*/reports', req => {
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
      }).as('reportMoment');

      cy.visit('/collection/my-moment');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getMyMoments');
      cy.wait('@getNotifications');
    });

    it('모멘트를 신고하면 해당 모멘트가 목록에서 사라진다', () => {
      const firstMoment = MOCK_MY_MOMENTS[0];
      cy.contains(firstMoment.content).should('be.visible');

      cy.get('button[class*="ComplaintButton"]').first().click();

      cy.contains('신고하기').should('be.visible');
      cy.contains('신고 사유를 선택해주세요').should('be.visible');

      cy.get('[role="dialog"]').within(() => {
        cy.contains('스팸/광고').click();
        cy.get('button').contains('신고하기').click();
      });

      cy.wait('@reportMoment');

      cy.contains('신고가 접수되었습니다').should('be.visible');

      cy.contains(firstMoment.content).should('not.exist');
    });
  });

  describe('시나리오 5: 모멘트 알림 확인', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      const mockNotifications = [
        {
          id: 1,
          type: 'MOMENT_REPLY',
          message: '누군가가 당신의 모멘트에 댓글을 남겼습니다.',
          isRead: false,
          createdAt: '2025-10-22T10:00:00',
        },
      ];

      cy.intercept('GET', '**/api/v1/notifications*', {
        statusCode: 200,
        body: {
          status: 200,
          data: mockNotifications,
        },
      }).as('getNotificationsWithData');

      cy.intercept('PATCH', '**/api/v1/notifications/*/read', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            success: true,
          },
        },
      }).as('markNotificationAsRead');

      cy.visit('/');

      cy.wait('@checkLogin');
      cy.wait('@getProfile');
      cy.wait('@getNotificationsWithData');
    });

    it('모멘트 관련 알림을 확인할 수 있다', () => {
      cy.get('button[class*="notification"]').click();

      cy.contains('누군가가 당신의 모멘트에 댓글을 남겼습니다.').should('be.visible');
    });
  });
});
