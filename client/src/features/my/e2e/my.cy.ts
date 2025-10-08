/// <reference types="cypress" />

const TEST_USER = {
  email: 'triton@moment.com',
  nickname: '따뜻한 마음의 트리톤',
  level: 'ASTEROID_WHITE',
  levelText: '소행성 1단계',
  expStar: 10,
  nextStepExp: 50,
  availableStar: 100,
  loginType: 'EMAIL',
} as const;

describe('마이페이지', () => {
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
          nickname: TEST_USER.nickname,
          level: TEST_USER.level,
          expStar: TEST_USER.expStar,
          nextStepExp: TEST_USER.nextStepExp,
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

    cy.intercept('GET', '**/api/v1/me/profile', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          nickname: TEST_USER.nickname,
          email: TEST_USER.email,
          level: TEST_USER.level,
          availableStar: TEST_USER.availableStar,
          expStar: TEST_USER.expStar,
          nextStepExp: TEST_USER.nextStepExp,
          loginType: TEST_USER.loginType,
        },
      },
    }).as('getMyProfile');

    cy.intercept('GET', '**/api/v1/me/reward/history?pageNum=0&pageSize=10', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          items: [],
          currentPageNum: 0,
          pageSize: 10,
          totalPages: 0,
        },
      },
    }).as('getRewardHistory');

    cy.visit('/my');
    cy.wait([
      '@checkLogin',
      '@getProfile',
      '@getNotifications',
      '@getMyProfile',
      '@getRewardHistory',
    ]);
  });

  describe('시나리오 1: 기본 정보 확인', () => {
    it('사용자 정보가 올바르게 표시된다', () => {
      cy.contains(TEST_USER.email).should('be.visible');
      cy.contains(TEST_USER.nickname).should('be.visible');
      cy.contains(TEST_USER.levelText).should('be.visible');
      cy.get('.current').should('contain', TEST_USER.expStar);
      cy.get('.total').should('contain', TEST_USER.nextStepExp);
      cy.contains(`사용가능한 별조각: ${TEST_USER.availableStar}`).should('be.visible');
    });
  });
});
