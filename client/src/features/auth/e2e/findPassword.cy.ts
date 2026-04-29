/// <reference types="cypress" />

const VALID_EMAIL = 'test@example.com';

const mockNotLoggedIn = () => {
  cy.intercept('GET', '**/api/v2/auth/login/check', {
    statusCode: 200,
    body: { status: 200, data: { isLogged: false } },
  }).as('checkLogin');
};

describe('비밀번호 찾기', () => {
  beforeEach(() => {
    mockNotLoggedIn();
    cy.visit('/find-password');
    cy.wait('@checkLogin');
  });

  describe('시나리오 1: 인증 링크 전송 성공', () => {
    it('이메일 입력 후 전송 버튼 클릭 시 성공 토스트가 표시된다', () => {
      cy.intercept('POST', '**/api/v2/auth/email/password', {
        statusCode: 200,
        body: { status: 200 },
      }).as('sendPasswordLink');

      cy.get('#email').type(VALID_EMAIL);
      cy.contains('인증 링크 전송하기').click();

      cy.wait('@sendPasswordLink');
      cy.contains('이메일로 인증 링크가 전송되었습니다').should('be.visible');
    });
  });

  describe('시나리오 2: 서버 에러', () => {
    it('존재하지 않는 이메일 입력 시 에러 토스트가 표시된다', () => {
      cy.intercept('POST', '**/api/v2/auth/email/password', {
        statusCode: 404,
        body: { status: 404, message: '존재하지 않는 이메일입니다.' },
      }).as('sendPasswordLinkFail');

      // 401 인터셉터가 refresh를 시도하지 않도록 (404는 해당 없음)
      cy.get('#email').type(VALID_EMAIL);
      cy.contains('인증 링크 전송하기').click();

      cy.wait('@sendPasswordLinkFail');
      cy.contains('이메일 인증에 실패했습니다').should('be.visible');
    });
  });

  describe('시나리오 3: 페이지 표시', () => {
    it('비밀번호 재발급 폼이 표시된다', () => {
      cy.contains('비밀번호 재발급').should('be.visible');
      cy.get('#email').should('be.visible');
      cy.contains('인증 링크 전송하기').should('be.visible');
    });
  });
});
