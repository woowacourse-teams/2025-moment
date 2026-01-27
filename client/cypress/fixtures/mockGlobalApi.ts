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

  cy.intercept('GET', /api\/v2\/(me\/profile|users\/me)/, {
    statusCode: 200,
    body: {
      status: 200,
      data: {
        email: userData.email,
        nickname: userData.nickname,
        loginType: userData.loginType,
      },
    },
  }).as('getProfile');

  cy.intercept('GET', '**/api/v2/notifications*', {
    statusCode: 200,
    body: {
      status: 200,
      data: [],
    },
  }).as('getNotifications');

  cy.intercept('GET', '**/api/v2/notifications/subscribe', {
    statusCode: 200,
    headers: {
      'content-type': 'text/event-stream',
    },
    body: '',
  }).as('notificationSubscribe');

  cy.intercept('GET', '**/api/v2/groups', {
    statusCode: 200,
    body: {
      status: 200,
      data: [
        {
          groupId: 1,
          name: '테스트 그룹',
          description: '테스트용 그룹입니다.',
          isOwner: true,
          memberCount: 5,
        },
      ],
    },
  }).as('getGroups');
};
