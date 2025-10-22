/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO = {
  id: 101,
  momenterId: 1,
  content: '오늘은 정말 행복한 하루였어요.',
  createdAt: '2025-10-22T09:00:00',
  imageUrl: null,
  tagNames: ['행복', '일상'],
  comments: [
    {
      id: 1,
      content: '정말 감동적인 이야기네요!',
      nickname: '별빛 가득한 아리아',
      level: 'ASTEROID_WHITE',
      createdAt: '2025-10-22T10:00:00',
      imageUrl: null,
      echos: [],
    },
  ],
  momentNotification: {
    isRead: false,
    notificationIds: [1001],
  },
} as const;

const MOCK_MOMENT_WITH_COMMENT_WITH_ECHO = {
  id: 102,
  momenterId: 1,
  content: '새로운 도전을 시작했어요!',
  createdAt: '2025-10-22T10:30:00',
  imageUrl: null,
  tagNames: ['성장'],
  comments: [
    {
      id: 2,
      content: '저도 비슷한 경험이 있어요.',
      nickname: '푸른 하늘의 테리우스',
      level: 'ASTEROID_WHITE',
      createdAt: '2025-10-22T11:00:00',
      imageUrl: null,
      echos: [
        {
          id: 1,
          echoType: 'THANKS',
          userId: 1,
        },
        {
          id: 2,
          echoType: 'TOUCHED',
          userId: 2,
        },
      ],
    },
  ],
  momentNotification: {
    isRead: true,
    notificationIds: [],
  },
} as const;

describe('에코(Echo) 기능', () => {
  describe('시나리오 1: 에코 전송', () => {
    beforeEach(() => {
      cy.intercept('GET', '**/api/v1/moments/me?*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            items: [MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO],
            hasNextPage: false,
            nextCursor: null,
          },
        },
      }).as('getMoments');

      cy.intercept('POST', '**/api/v1/echos', req => {
        expect(req.body).to.have.property('echoTypes');
        expect(req.body).to.have.property('commentId');
        expect(req.body.commentId).to.equal(MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO.comments[0].id);
        expect(req.body.echoTypes).to.be.an('array');
        expect(req.body.echoTypes.length).to.be.greaterThan(0);

        req.reply({
          statusCode: 201,
          body: {
            status: 201,
            data: req.body.echoTypes.map((type: string, index: number) => ({
              id: 100 + index,
              echoType: type,
            })),
          },
        });
      }).as('sendEcho');

      mockGlobalAPIs();

      cy.visit('/collection/my-moment');
      cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getMoments']);
    });

    it('이모지를 선택하고 에코를 전송할 수 있다', () => {
      cy.contains(MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO.content).click();
      cy.contains(MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO.comments[0].content).should('be.visible');
      cy.contains('에코 보내기').should('be.visible');
      cy.contains('마음 고마워요').click();
      cy.contains('감동 받았어요').click();
      cy.get('button').contains('전송').should('not.be.disabled');
      cy.get('button').contains('전송').click();
      cy.wait('@sendEcho').then(interception => {
        expect(interception.request.body.echoTypes).to.include('THANKS');
        expect(interception.request.body.echoTypes).to.include('TOUCHED');
      });
      cy.contains('에코가 성공적으로 전송되었습니다!', { timeout: 5000 }).should('be.visible');
    });

    it('에코를 선택하지 않으면 전송 버튼이 비활성화된다', () => {
      cy.contains(MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO.content).click();
      cy.contains('전송').should('be.visible');
      cy.get('button').contains('전송').should('be.disabled');
    });

    it('여러 개의 에코를 동시에 선택하여 전송할 수 있다', () => {
      cy.contains(MOCK_MOMENT_WITH_COMMENT_WITHOUT_ECHO.content).click();
      cy.contains('마음 고마워요').click();
      cy.contains('재밌어요').click();
      cy.contains('위로가 됐어요').click();
      cy.get('button').contains('전송').click();
      cy.wait('@sendEcho').then(interception => {
        expect(interception.request.body.echoTypes).to.have.lengthOf(3);
        expect(interception.request.body.echoTypes).to.include.members([
          'THANKS',
          'FUNNY',
          'COMFORTED',
        ]);
      });
    });
  });
});
