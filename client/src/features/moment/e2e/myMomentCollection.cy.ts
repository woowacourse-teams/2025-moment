/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const momentsData = [
  {
    id: 1,
    momentId: 1,
    momenterId: 1,
    memberId: 1,
    content: '오늘 정말 행복한 하루를 보냈어요!',
    memberNickname: '테스트유저',
    createdAt: '2025-10-22T10:00:00',
    imageUrl: null,
    comments: [
      {
        id: 101,
        content: '정말 좋은 하루 보내셨네요!',
        nickname: '따뜻한 햇살의 아리아',
        memberNickname: '따뜻한 햇살의 아리아',
        createdAt: '2025-10-22T10:30:00',
        imageUrl: null,
      },
    ],
    momentNotification: {
      isRead: false,
      notificationIds: [1001],
    },
  },
] as const;

describe('나의 모멘트 모음집 페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    cy.intercept('GET', '**/api/v2/groups/1/my-moments*', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          moments: momentsData,
          nextCursor: null,
          hasNextPage: false,
          pageSize: 10,
        },
      },
    }).as('getMyMoments');

    cy.intercept('GET', '**/api/v2/groups/1/my-moments/unread*', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          moments: [],
          nextCursor: null,
          hasNextPage: false,
          pageSize: 10,
        },
      },
    }).as('getUnreadMoments');

    cy.visit('/groups/1/collection/my-moment');
    cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getMyMoments']);
  });

  describe('시나리오 1: 모멘트 목록 확인', () => {
    it('내가 작성한 모멘트 목록을 볼 수 있다', () => {
      cy.contains('나의 모멘트').should('be.visible');
      cy.contains(momentsData[0].content).should('be.visible');
    });
  });

  describe('시나리오 2: 상세 동작', () => {
    it('코멘트가 있는 모멘트를 클릭하면 모달이 열린다', () => {
      cy.contains(momentsData[0].content).click();
      cy.get('[role="dialog"]').should('be.visible');
      cy.contains('정말 좋은 하루 보내셨네요!').should('be.visible');
    });

    it('코멘트를 신고할 수 있다', () => {
      cy.intercept('POST', '**/api/v2/comments/*/reports', {
        statusCode: 201,
        body: { status: 201, data: { id: 1 } },
      }).as('reportComment');

      cy.contains(momentsData[0].content).click();

      // 신고 버튼 클릭 (Siren 아이콘을 포함한 버튼)
      cy.get('button[aria-label="코멘트 신고하기"]').first().click();

      cy.get('[role="dialog"]')
        .last()
        .within(() => {
          cy.contains('스팸/광고').click();
          cy.get('button').contains('신고하기').click();
        });

      cy.wait('@reportComment');
      cy.contains('신고가 접수되었습니다').should('be.visible');
    });
  });
});
