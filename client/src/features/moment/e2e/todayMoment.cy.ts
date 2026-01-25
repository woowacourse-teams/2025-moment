/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const TEST_MOMENT = {
  content: '오늘 정말 행복한 하루였어요!',
} as const;

describe('오늘의 모멘트 페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    cy.intercept('POST', '**/api/v2/groups/1/moments', {
      statusCode: 201,
      body: {
        status: 201,
        data: { id: 1, content: TEST_MOMENT.content },
      },
    }).as('sendMoment');

    cy.visit('/groups/1/today-moment');
    cy.wait(['@checkLogin', '@getProfile', '@getNotifications']);
  });

  it('텍스트를 입력하고 모멘트를 작성하면 성공 페이지로 이동한다', () => {
    cy.contains('오늘의 모멘트').should('be.visible');
    cy.get('textarea').clear().type(TEST_MOMENT.content);
    cy.get('button').contains('모멘트 공유하기').click();

    cy.wait('@sendMoment');
    cy.url().should('include', '/today-moment/success');
  });
});
