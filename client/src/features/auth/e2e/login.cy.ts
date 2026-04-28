/// <reference types="cypress" />

const VALID_EMAIL = 'test@example.com';
const VALID_PASSWORD = 'Valid123!';

const mockNotLoggedIn = () => {
  cy.intercept('GET', '**/api/v2/auth/login/check', {
    statusCode: 200,
    body: { status: 200, data: { isLogged: false } },
  }).as('checkLogin');

  // 401 인터셉터가 토큰 갱신을 시도하므로 mock 필요
  cy.intercept('POST', '**/api/v2/auth/refresh', {
    statusCode: 401,
    body: { message: 'Refresh token expired' },
  }).as('refreshToken');
};

describe('로그인', () => {
  beforeEach(() => {
    mockNotLoggedIn();
    cy.visit('/login');
    cy.wait('@checkLogin');
    // isLogged: false → checkIfLoggined이 /auth/refresh 호출 → 초기 refresh call 소비
    cy.wait('@refreshToken');
  });

  describe('시나리오 1: 로그인 성공', () => {
    it('유효한 이메일·비밀번호 입력 후 로그인하면 홈으로 이동한다', () => {
      cy.intercept('POST', '**/api/v2/auth/login', {
        statusCode: 200,
        body: { status: 200, data: {} },
      }).as('login');

      cy.get('#email').type(VALID_EMAIL);
      cy.get('#password').type(VALID_PASSWORD);
      cy.get('button[type="submit"]').click();

      cy.wait('@login');
      cy.url().should('eq', Cypress.config().baseUrl + '/');
    });
  });

  describe('시나리오 2: 유효성 검사', () => {
    it('잘못된 이메일 형식 입력 시 에러 메시지가 표시된다', () => {
      cy.get('#email').type('invalid-email');
      cy.get('#password').type(VALID_PASSWORD);
      cy.contains('올바른 이메일 형식을 입력해주세요.').should('be.visible');
    });

    it('조건에 맞지 않는 비밀번호 입력 시 에러 메시지가 표시된다', () => {
      cy.get('#email').type(VALID_EMAIL);
      cy.get('#password').type('weak');
      cy.contains('비밀번호는 8-16자의 영문 소문자, 숫자, 특수문자').should('be.visible');
    });
  });

  describe('시나리오 3: 서버 에러', () => {
    it('로그인 실패 시 에러 토스트가 표시되고 로그인 페이지에 머문다', () => {
      cy.intercept('POST', '**/api/v2/auth/login', {
        statusCode: 401,
        body: { status: 401, message: '이메일 또는 비밀번호가 올바르지 않습니다.' },
      }).as('loginFail');

      cy.get('#email').type(VALID_EMAIL);
      cy.get('#password').type(VALID_PASSWORD);
      cy.get('button[type="submit"]').click();

      cy.wait('@loginFail');
      cy.wait('@refreshToken');
      cy.url().should('include', '/login');
      // 401 → 토큰 갱신 시도 → 갱신도 실패 → interceptor가 "로그인이 만료되었어요" 토스트 표시
      cy.contains('로그인이 만료되었어요', { timeout: 5000 }).should('be.visible');
    });
  });

  describe('시나리오 4: 페이지 이동', () => {
    it('회원가입 링크 클릭 시 회원가입 페이지로 이동한다', () => {
      cy.contains('회원가입').click();
      cy.url().should('include', '/signup');
    });

    it('비밀번호 찾기 클릭 시 비밀번호 찾기 페이지로 이동한다', () => {
      cy.contains('비밀번호 찾기').click();
      cy.url().should('include', '/find-password');
    });
  });
});
