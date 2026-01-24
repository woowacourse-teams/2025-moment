/// <reference types="cypress" />

import { userData } from './userData';

export const mockGlobalAPIs = () => {
  cy.intercept('GET', '**/api/v2/auth/login/check', {
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
        nickname: userData.nickname,
        level: userData.level,
        expStar: userData.expStar,
        nextStepExp: userData.nextStepExp,
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

  cy.intercept('GET', '**/api/v1/notifications/subscribe', {
    statusCode: 200,
    headers: {
      'content-type': 'text/event-stream',
    },
    body: '',
  }).as('notificationSubscribe');
};
