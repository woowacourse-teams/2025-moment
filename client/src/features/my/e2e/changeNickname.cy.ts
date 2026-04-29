/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';
import { userData } from '../../../../cypress/fixtures/userData';

const NEW_NICKNAME = '설레는 고양이';

describe('닉네임 변경', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    // 랜덤 닉네임 API mock
    cy.intercept('GET', '**/api/v2/users/signup/nickname', {
      statusCode: 200,
      body: { status: 200, data: { randomNickname: NEW_NICKNAME } },
    }).as('getRandomNickname');

    // 닉네임 변경 API mock
    cy.intercept('POST', '**/api/v2/me/nickname', {
      statusCode: 200,
      body: { status: 200 },
    }).as('changeNickname');

    cy.intercept('GET', '**/api/v2/groups*', {
      statusCode: 200,
      body: { status: 200, data: [] },
    }).as('getMyGroups');

    // 차단 목록 API mock (미등록 시 401 → refresh 루프 → /login 리다이렉트 발생)
    cy.intercept('GET', '**/api/v2/users/blocks*', {
      statusCode: 200,
      body: { status: 200, data: [] },
    }).as('getBlockList');

    cy.visit('/my');
    cy.wait(['@checkLogin', '@getProfile', '@getNotifications']);
    cy.contains(userData.nickname).should('be.visible');
  });

  describe('시나리오 1: 닉네임 변경 성공', () => {
    it('닉네임 변경 버튼 클릭 시 변경 모달이 열린다', () => {
      cy.contains('닉네임 변경').click();
      cy.wait('@getRandomNickname');
      cy.get('#nickname').should('be.visible');
    });

    it('랜덤 닉네임이 표시된 상태에서 변경하기를 클릭하면 성공 토스트가 표시된다', () => {
      cy.contains('닉네임 변경').click();
      cy.wait('@getRandomNickname');

      cy.get('#nickname').should('have.value', NEW_NICKNAME);
      cy.contains('변경하기').click();

      cy.wait('@changeNickname');
      cy.contains('닉네임이 변경되었습니다.').should('be.visible');
    });

    it('닉네임 변경 후 모달이 닫힌다', () => {
      cy.contains('닉네임 변경').click();
      cy.wait('@getRandomNickname');

      cy.contains('변경하기').click();
      cy.wait('@changeNickname');

      cy.get('#nickname').should('not.exist');
    });
  });

  describe('시나리오 2: 닉네임 새로고침', () => {
    it('회전 버튼 클릭 시 새로운 랜덤 닉네임을 가져온다', () => {
      cy.contains('닉네임 변경').click();
      cy.wait('@getRandomNickname');

      const ANOTHER_NICKNAME = '용감한 사자';
      cy.intercept('GET', '**/api/v2/users/signup/nickname', {
        statusCode: 200,
        body: { status: 200, data: { randomNickname: ANOTHER_NICKNAME } },
      }).as('getAnotherNickname');

      cy.get('[aria-label="다른 닉네임으로 변경"]').click();
      cy.wait('@getAnotherNickname');
      cy.get('#nickname').should('have.value', ANOTHER_NICKNAME);
    });
  });
});
