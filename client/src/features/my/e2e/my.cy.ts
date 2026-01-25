/// <reference types="cypress" />

import { userData } from '../../../../cypress/fixtures/userData';
import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

describe('마이페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    cy.intercept('GET', '**/api/v2/me/profile', {
      statusCode: 200,
      body: {
        status: 200,
        data: userData,
      },
    }).as('getMyProfile');

    cy.intercept('GET', '**/api/v2/me/reward/history?pageNum=0&pageSize=10', {
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
      cy.contains(userData.email).should('be.visible');
      cy.contains(userData.nickname).should('be.visible');
      cy.contains(userData.levelText).should('be.visible');
      cy.get('.current').should('contain', userData.expStar);
      cy.get('.total').should('contain', userData.nextStepExp);
      cy.contains(`사용가능한 별조각: ${userData.availableStar}`).should('be.visible');
    });
  });
});
