/// <reference types="cypress" />

import { userData } from '../../../../cypress/fixtures/userData';
import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

describe('마이페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    cy.visit('/my');
    cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getGroups']);
  });

  describe('시나리오 1: 기본 정보 확인', () => {
    it('사용자 정보가 올바르게 표시된다', () => {
      cy.contains(userData.email).should('be.visible');
      cy.contains(userData.nickname).should('be.visible');
    });

    it('내 그룹 정보가 표시된다', () => {
      cy.contains('내 그룹 (1)').should('be.visible');
      cy.contains('테스트 그룹').should('be.visible');
    });
  });
});
