/// <reference types="cypress" />

import { COMPLAINT_REASONS } from '../../complaint/types/complaintType';
import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const momentsData = [
  {
    id: 1,
    nickname: '푸르른 물방울의 테리우스',
    level: 'ASTEROID_WHITE',
    content: '오늘 정말 힘든 하루였어요. 그래도 버텨냈네요.',
    imageUrl: null,
    createdAt: '2025-01-20T10:00:00',
  },
  {
    id: 2,
    nickname: '따뜻한 햇살의 아리아',
    level: 'ASTEROID_WHITE',
    content: '새로운 하루가 시작되었어요. 오늘도 화이팅!',
    imageUrl: null,
    createdAt: '2025-01-20T11:00:00',
  },
  {
    id: 3,
    nickname: '빛나는 별의 루나',
    level: 'ASTEROID_WHITE',
    content: '오늘도 좋은 하루를 보내고 있어요!',
    imageUrl: null,
    createdAt: '2025-01-20T12:00:00',
  },
] as const;

const TEST_COMMENT = {
  id: 123,
  content: '오늘 정말 수고 많으셨어요.',
  createdAt: '2025-01-20T10:05:00',
  commentImageId: null,
} as const;

describe('오늘의 코멘트 페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    let momentCallCount = 0;
    cy.intercept('GET', '**/api/v2/groups/1/moments/commentable*', req => {
      const momentData = momentsData[momentCallCount % momentsData.length];
      momentCallCount++;

      req.reply({
        statusCode: 200,
        body: {
          status: 200,
          data: momentData,
        },
      });
    }).as('getMoments');

    cy.intercept('POST', '**/api/v2/groups/1/moments/*/comments', {
      statusCode: 201,
      body: {
        status: 201,
        data: TEST_COMMENT,
      },
    }).as('sendComment');

    cy.visit('/groups/1/today-comment');

    cy.wait('@checkLogin');
    cy.wait('@getProfile');
    cy.wait('@getMoments');
    cy.wait('@getNotifications');
  });

  describe('시나리오 1: 코멘트 보내기', () => {
    it('다른 사용자 모멘트에 댓글을 작성하고 코멘트 보내기 버튼을 클릭하면 별조각 획득 토스트가 보인다', () => {
      cy.contains('오늘의 코멘트').should('be.visible');

      cy.contains(momentsData[0].nickname).should('be.visible');
      cy.contains(momentsData[0].content).should('be.visible');

      cy.get('textarea').should('be.visible');
      cy.get('textarea').should('have.attr', 'placeholder', '따뜻한 위로의 말을 전해주세요...');
      cy.get('textarea').clear().type(TEST_COMMENT.content);

      cy.get('button').contains('코멘트 보내기').should('not.be.disabled');
      cy.get('button').contains('코멘트 보내기').click();

      cy.wait('@sendComment');

      cy.contains('별조각', { timeout: 5000 }).should('be.visible');

      cy.wait('@getMoments');

      cy.contains(momentsData[0].nickname).should('not.exist');
      cy.contains(momentsData[0].content).should('not.exist');

      cy.contains(momentsData[1].nickname).should('be.visible');
      cy.contains(momentsData[1].content).should('be.visible');

      cy.get('textarea').should('have.value', '');
    });

    it('빈 댓글로는 코멘트를 보낼 수 없다', () => {
      cy.get('textarea').should('have.value', '');
      cy.get('button').contains('코멘트 보내기').should('be.disabled');
    });
  });

  describe('시나리오 2: 리롤 버튼', () => {
    it('리롤 버튼을 클릭하면 다른 모멘트가 보인다', () => {
      cy.contains(momentsData[0].nickname).should('be.visible');
      cy.contains(momentsData[0].content).should('be.visible');

      cy.get('button[class*="RefreshButton"]').click();

      cy.wait('@getMoments');

      cy.contains(momentsData[0].nickname).should('not.exist');
      cy.contains(momentsData[0].content).should('not.exist');
      cy.contains(momentsData[1].nickname).should('be.visible');
      cy.contains(momentsData[1].content).should('be.visible');

      cy.get('textarea').should('have.value', '');
      cy.get('button').contains('코멘트 보내기').should('be.disabled');
    });
  });

  describe('시나리오 3: 신고하기', () => {
    beforeEach(() => {
      cy.intercept('POST', '**/api/v2/moments/*/reports', req => {
        expect(req.body).to.have.property('reason');
        expect(req.body.reason).to.be.oneOf(Object.values(COMPLAINT_REASONS));

        req.reply({
          statusCode: 201,
          body: {
            id: 1,
            reason: req.body.reason,
          },
        });
      }).as('momentComplaint');

      cy.intercept('GET', '**/api/v2/groups/1/moments/commentable*', {
        statusCode: 200,
        body: {
          status: 200,
          data: momentsData[1],
        },
      }).as('getNewMoments');
    });

    it('신고 아이콘을 클릭하면 신고하기 모달이 보인다', () => {
      cy.get('button[class*="ComplaintButton"]').click();

      cy.contains('신고하기').should('be.visible');
      cy.contains('신고 사유를 선택해주세요').should('be.visible');

      cy.get('[role="dialog"]').within(() => {
        cy.contains('스팸/광고').should('exist');
        cy.contains('선정적 콘텐츠').should('exist');
        cy.contains('혐오 발언/차별').should('exist');
        cy.contains('괴롭힘/악플').should('exist');

        cy.get('button').contains('신고하기').should('be.disabled');
      });
    });

    it('리스트 중 하나를 클릭하고 신고하기 버튼을 클릭하면 모달이 꺼지면서 신고 접수 토스트가 보인다', () => {
      cy.get('button[class*="ComplaintButton"]').click();

      cy.get('[role="dialog"]').within(() => {
        cy.contains('스팸/광고').click();

        cy.get('button').contains('신고하기').should('not.be.disabled');
        cy.get('button').contains('신고하기').click();
      });

      cy.wait('@momentComplaint');

      cy.contains('신고하기').should('not.exist');

      cy.contains('신고가 접수되었습니다', { timeout: 4000 }).should('be.visible');
    });

    it('신고를 누르면 해당 모멘트가 사라진다', () => {
      cy.contains(momentsData[0].nickname).should('be.visible');
      cy.contains(momentsData[0].content).should('be.visible');

      cy.get('button[class*="ComplaintButton"]').click();

      cy.get('[role="dialog"]').within(() => {
        cy.contains('스팸/광고').click();
        cy.get('button').contains('신고하기').click();
      });

      cy.wait('@momentComplaint');
      cy.wait('@getNewMoments');

      cy.contains(momentsData[0].nickname).should('not.exist');
      cy.contains(momentsData[0].content).should('not.exist');

      cy.contains(momentsData[1].nickname).should('be.visible');
      cy.contains(momentsData[1].content).should('be.visible');
    });

    it('모달에서 취소 버튼을 클릭하면 모달이 닫힌다', () => {
      cy.get('button[class*="ComplaintButton"]').click();

      cy.contains('신고하기').should('be.visible');

      cy.get('[role="dialog"]').within(() => {
        cy.get('button').contains('취소').click();
      });

      cy.contains('신고하기').should('not.exist');
      cy.contains('신고 사유를 선택해주세요').should('not.exist');
    });

    it('모달 외부를 클릭하면 모달이 닫힌다', () => {
      cy.get('button[class*="ComplaintButton"]').click();

      cy.contains('신고하기').should('be.visible');

      cy.get('body').click(0, 0);

      cy.contains('신고하기').should('not.exist');
    });
  });
});
