/// <reference types="cypress" />

const TEST_EMAIL = 'newuser@example.com';
const TEST_PASSWORD = 'Valid123!';
const TEST_CODE = '123456';
const TEST_NICKNAME = '행복한 판다';

const EMAIL_CODE_SEND_BTN = '[aria-label="이메일 인증코드 전송"]';
const EMAIL_CODE_VERIFY_BTN = '[aria-label="이메일 인증코드 확인"]';
const NEXT_BTN = '[aria-label="다음 단계로"]';
const PREV_BTN = '[aria-label="이전 단계로"]';
const SIGNUP_BTN = 'button[aria-label="회원가입"]';

const mockNotLoggedIn = () => {
  cy.intercept('GET', '**/api/v2/auth/login/check', {
    statusCode: 200,
    body: { status: 200, data: { isLogged: false } },
  }).as('checkLogin');

  // isLogged: false → checkIfLoggined이 /auth/refresh 실제 호출 시도 → CI에서 10s 대기 발생 방지
  cy.intercept('POST', '**/api/v2/auth/refresh', {
    statusCode: 401,
    body: { message: 'Refresh token expired' },
  }).as('refreshToken');

  // NavigatorsBar의 useGroupsQuery: 미mock 시 401 → api.ts interceptor refresh 경쟁 발생 방지
  cy.intercept('GET', '**/api/v2/groups*', {
    statusCode: 200,
    body: { status: 200, data: [] },
  }).as('getGroups');
};

const mockSignupAPIs = () => {
  cy.intercept('POST', '**/api/v2/auth/email', {
    statusCode: 200,
    body: { status: 200 },
  }).as('sendEmailCode');

  cy.intercept('POST', '**/api/v2/auth/email/verify', {
    statusCode: 200,
    body: { status: 200 },
  }).as('verifyEmailCode');

  cy.intercept('GET', '**/api/v2/users/signup/nickname', {
    statusCode: 200,
    body: { status: 200, data: { randomNickname: TEST_NICKNAME } },
  }).as('getRandomNickname');

  cy.intercept('POST', '**/api/v2/users/signup', {
    statusCode: 201,
    body: { status: 201 },
  }).as('signup');
};

// Step1 공통 처리 헬퍼
const completeStep1 = () => {
  cy.get('#email').type(TEST_EMAIL);
  cy.get(EMAIL_CODE_SEND_BTN).click();
  cy.wait('@sendEmailCode');
  cy.get('#emailCode').type(TEST_CODE);
  cy.get(EMAIL_CODE_VERIFY_BTN).click();
  cy.wait('@verifyEmailCode');
  cy.get('#password').type(TEST_PASSWORD);
  cy.get('#rePassword').type(TEST_PASSWORD);
  cy.get(NEXT_BTN).click();
};

describe('회원가입', () => {
  beforeEach(() => {
    mockNotLoggedIn();
    mockSignupAPIs();
    cy.visit('/signup');
    cy.wait('@checkLogin');
  });

  describe('시나리오 1: 전체 회원가입 플로우', () => {
    it('Step1 - 이메일 인증 후 비밀번호 입력하면 다음 단계로 이동한다', () => {
      cy.get('#email').type(TEST_EMAIL);
      cy.get(EMAIL_CODE_SEND_BTN).click();
      cy.wait('@sendEmailCode');
      cy.contains('인증코드가 전송되었습니다.').should('be.visible');

      cy.get('#emailCode').type(TEST_CODE);
      cy.get(EMAIL_CODE_VERIFY_BTN).click();
      cy.wait('@verifyEmailCode');
      cy.contains('인증코드가 확인되었습니다.').should('be.visible');

      cy.get('#password').type(TEST_PASSWORD);
      cy.get('#rePassword').type(TEST_PASSWORD);
      cy.get(NEXT_BTN).should('not.be.disabled').click();

      cy.wait('@getRandomNickname');
      cy.get('#nickname').should('be.visible');
    });

    it('Step2 - 랜덤 닉네임이 표시되고 다음 단계로 이동한다', () => {
      completeStep1();
      cy.wait('@getRandomNickname');
      cy.get('#nickname').should('have.value', TEST_NICKNAME);
      cy.get(NEXT_BTN).click();

      cy.contains(TEST_EMAIL).should('be.visible');
      cy.contains(TEST_NICKNAME).should('be.visible');
    });

    it('Step3 - 이용약관 동의 후 회원가입 완료하면 로그인 페이지로 이동한다', () => {
      completeStep1();
      cy.wait('@getRandomNickname');
      cy.get(NEXT_BTN).click();

      cy.contains('이용약관에 동의합니다').should('be.visible');
      cy.get('input[type="checkbox"]').check();
      cy.get(SIGNUP_BTN).should('not.be.disabled').click();
      cy.wait('@signup');

      cy.contains('회원가입이 완료되었습니다').should('be.visible');
      cy.url().should('include', '/login');
    });
  });

  describe('시나리오 2: 유효성 검사', () => {
    it('이메일 형식이 틀리면 인증코드 전송 버튼이 비활성화된다', () => {
      cy.get('#email').type('not-an-email');
      cy.get(EMAIL_CODE_SEND_BTN).should('be.disabled');
    });

    it('인증코드가 6자리가 아니면 인증코드 확인 버튼이 비활성화된다', () => {
      cy.get('#emailCode').type('123');
      cy.get(EMAIL_CODE_VERIFY_BTN).should('be.disabled');
    });

    it('비밀번호와 비밀번호 확인이 다르면 에러 메시지가 표시된다', () => {
      cy.get('#password').type(TEST_PASSWORD);
      cy.get('#rePassword').type('Different1!');
      cy.contains('비밀번호가 일치하지 않습니다.').should('be.visible');
    });

    it('이용약관 미동의 시 회원가입 버튼이 비활성화된다', () => {
      completeStep1();
      cy.wait('@getRandomNickname');
      cy.get(NEXT_BTN).click();

      cy.get(SIGNUP_BTN).should('be.disabled');
    });
  });

  describe('시나리오 3: 이전 단계 이동', () => {
    it('Step2에서 이전 버튼 클릭 시 Step1로 돌아간다', () => {
      completeStep1();
      cy.wait('@getRandomNickname');

      cy.get(PREV_BTN).click();

      cy.get('#email').should('be.visible');
      cy.get(EMAIL_CODE_SEND_BTN).should('be.visible');
    });
  });
});
