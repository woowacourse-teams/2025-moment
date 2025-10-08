/// <reference types="cypress" />

describe('오늘의 코멘트 페이지', () => {
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

    let momentCallCount = 0;
    cy.intercept('GET', '**/api/v1/moments/commentable*', req => {
      momentCallCount++;

      const momentData =
        momentCallCount === 1
          ? {
              id: 1,
              nickname: '푸르른 물방울의 테리우스',
              level: 'ASTEROID_WHITE',
              content: '오늘 정말 힘든 하루였어요. 그래도 버텨냈네요.',
              imageUrl: null,
              createdAt: '2025-01-20T10:00:00',
            }
          : {
              id: 2,
              nickname: '따뜻한 햇살의 아리아',
              level: 'ASTEROID_WHITE',
              content: '새로운 하루가 시작되었어요. 오늘도 화이팅!',
              imageUrl: null,
              createdAt: '2025-01-20T11:00:00',
            };

      req.reply({
        statusCode: 200,
        body: {
          status: 200,
          data: momentData,
        },
      });
    }).as('getMoments');

    cy.intercept('POST', '**/api/v1/comments', {
      statusCode: 201,
      body: {
        status: 201,
        data: {
          commentId: 123,
          content: '오늘 정말 수고 많으셨어요.',
          createdAt: '2025-01-20T10:05:00',
          commentImageId: null,
        },
      },
    }).as('sendComment');

    cy.visit('/today-comment');

    cy.wait('@checkLogin');
    cy.wait('@getProfile');
    cy.wait('@getMoments');
    cy.wait('@getNotifications');
  });

  describe('시나리오 1: 코멘트 보내기', () => {
    it('다른 사용자 모멘트에 댓글을 작성하고 코멘트 보내기 버튼을 클릭하면 별조각 획득 토스트가 보인다', () => {
      cy.contains('오늘의 코멘트').should('be.visible');

      cy.contains('푸르른 물방울의 테리우스').should('be.visible');
      cy.contains('오늘 정말 힘든 하루였어요').should('be.visible');

      cy.get('textarea').should('be.visible');
      cy.get('textarea').should('have.attr', 'placeholder', '따뜻한 위로의 말을 전해주세요...');
      cy.get('textarea').clear().type('오늘 정말 수고 많으셨어요.');

      cy.get('button').contains('코멘트 보내기').should('not.be.disabled');
      cy.get('button').contains('코멘트 보내기').click();

      cy.wait('@sendComment');

      cy.contains('별조각', { timeout: 5000 }).should('be.visible');

      cy.wait('@getMoments');

      cy.contains('푸르른 물방울의 테리우스').should('not.exist');
      cy.contains('오늘 정말 힘든 하루였어요').should('not.exist');

      cy.contains('따뜻한 햇살의 아리아').should('be.visible');
      cy.contains('새로운 하루가 시작되었어요. 오늘도 화이팅!').should('be.visible');

      cy.get('textarea').should('have.value', '');
    });

    it('빈 댓글로는 코멘트를 보낼 수 없다', () => {
      cy.get('textarea').should('have.value', '');
      cy.get('button').contains('코멘트 보내기').should('be.disabled');
    });
  });
});
