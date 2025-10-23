/// <reference types="cypress" />

import { mockGlobalAPIs } from '../../../../cypress/fixtures/mockGlobalApi';

const momentsData = [
  {
    id: 1,
    momenterId: 1,
    content: '오늘 정말 행복한 하루를 보냈어요!',
    createdAt: '2025-10-22T10:00:00',
    imageUrl: null,
    tagNames: ['행복', '일상'],
    comments: [
      {
        id: 101,
        content: '정말 좋은 하루 보내셨네요!',
        nickname: '따뜻한 햇살의 아리아',
        level: 'ASTEROID_WHITE',
        createdAt: '2025-10-22T10:30:00',
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
      {
        id: 102,
        content: '부럽습니다!',
        nickname: '푸른 하늘의 테리우스',
        level: 'ASTEROID_WHITE',
        createdAt: '2025-10-22T11:00:00',
        imageUrl: null,
        echos: [],
      },
    ],
    momentNotification: {
      isRead: false,
      notificationIds: [1001, 1002],
    },
  },
  {
    id: 2,
    momenterId: 1,
    content: '새로운 도전을 시작했어요!',
    createdAt: '2025-10-22T12:00:00',
    imageUrl: 'https://example.com/image.jpg',
    tagNames: ['성장', '도전'],
    comments: [
      {
        id: 103,
        content: '응원합니다!',
        nickname: '빛나는 별의 루나',
        level: 'ASTEROID_WHITE',
        createdAt: '2025-10-22T12:30:00',
        imageUrl: null,
        echos: [
          {
            id: 3,
            echoType: 'AMAZING',
            userId: 3,
          },
        ],
      },
    ],
    momentNotification: {
      isRead: true,
      notificationIds: [],
    },
  },
  {
    id: 3,
    momenterId: 1,
    content: '조용한 하루였어요.',
    createdAt: '2025-10-22T13:00:00',
    imageUrl: null,
    tagNames: ['휴식'],
    comments: null,
    momentNotification: {
      isRead: true,
      notificationIds: [],
    },
  },
] as const;

describe('나의 모멘트 모음집 페이지', () => {
  beforeEach(() => {
    mockGlobalAPIs();

    cy.intercept('GET', '**/api/v1/moments/me?*', {
      statusCode: 200,
      body: {
        status: 200,
        data: {
          items: momentsData,
          nextCursor: null,
          hasNextPage: false,
          pageSize: 10,
        },
      },
    }).as('getMyMoments');

    cy.visit('/collection/my-moment');
    cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getMyMoments']);
  });

  describe('시나리오 1: 모멘트 목록 확인', () => {
    it('내가 작성한 모멘트 목록을 볼 수 있다', () => {
      cy.contains('나의 모멘트').should('be.visible');
      cy.contains('내가 공유한 모멘트와 받은 코멘트를 확인해보세요').should('be.visible');

      cy.contains(momentsData[0].content).should('be.visible');
      cy.contains(momentsData[1].content).should('be.visible');
      cy.contains(momentsData[2].content).should('be.visible');
    });

    it('모멘트의 태그를 확인할 수 있다', () => {
      momentsData.forEach(moment => {
        moment.tagNames.forEach(tag => {
          cy.contains(tag).should('be.visible');
        });
      });
    });
  });

  describe('시나리오 2: 코멘트가 있는 모멘트', () => {
    it('코멘트가 있는 모멘트는 클릭할 수 있다', () => {
      cy.contains(momentsData[0].content)
        .closest('[class*="MyMomentsCard"]')
        .should('have.css', 'cursor', 'pointer');
    });

    it('코멘트 수를 확인할 수 있다', () => {
      const firstMoment = momentsData[0];
      const commentCount = firstMoment.comments?.length || 0;

      cy.contains(firstMoment.content)
        .closest('[class*="MyMomentsCard"]')
        .within(() => {
          cy.contains(commentCount.toString()).should('be.visible');
        });
    });
  });

  describe('시나리오 3: 코멘트 상세 모달', () => {
    it('코멘트가 있는 모멘트를 클릭하면 모달이 열린다', () => {
      cy.contains(momentsData[0].content).click();

      cy.get('[role="dialog"]').should('be.visible');

      const firstComment = momentsData[0].comments?.[momentsData[0].comments.length - 1];
      if (firstComment) {
        cy.contains(firstComment.content).should('be.visible');
        cy.contains(firstComment.nickname).should('be.visible');
      }
    });

    it('모달에서 여러 코멘트를 네비게이션할 수 있다', () => {
      const momentWithMultipleComments = momentsData[0];
      cy.contains(momentWithMultipleComments.content).click();

      cy.get('[role="dialog"]').should('be.visible');

      const commentsReversed = [...(momentWithMultipleComments.comments || [])].reverse();

      cy.contains(commentsReversed[0].content).should('be.visible');

      if (commentsReversed.length > 1) {
        cy.get('button[class*="CommentNavigationButton"]').last().click();

        cy.contains(commentsReversed[1].content).should('be.visible');

        cy.get('button[class*="CommentNavigationButton"]').first().click();

        cy.contains(commentsReversed[0].content).should('be.visible');
      }
    });

    it('모달을 닫을 수 있다', () => {
      cy.contains(momentsData[0].content).click();

      cy.get('[role="dialog"]').should('be.visible');

      cy.get('button[aria-label="닫기"], button[class*="CloseButton"]')
        .should('be.visible')
        .should('not.be.disabled')
        .click({ force: true });

      // 모달이 DOM에서 제거될 때까지 대기 (애니메이션 + 제거 시간 고려)
      cy.get('[role="dialog"]').should('not.exist');
    });
  });

  describe('시나리오 4: 에코 확인', () => {
    it('코멘트에 받은 에코를 확인할 수 있다', () => {
      cy.contains(momentsData[0].content).click();

      cy.get('[role="dialog"]').should('be.visible');

      const firstComment = momentsData[0].comments?.[momentsData[0].comments.length - 1];
      if (firstComment && firstComment.echos.length > 0) {
        cy.contains('보낸 에코').should('be.visible');
      }
    });
  });

  describe('시나리오 5: 코멘트 신고', () => {
    beforeEach(() => {
      cy.intercept('POST', '**/api/v1/comments/*/reports', req => {
        expect(req.body).to.have.property('reason');

        req.reply({
          statusCode: 201,
          body: {
            status: 201,
            data: {
              id: 1,
              reason: req.body.reason,
            },
          },
        });
      }).as('reportComment');
    });

    it('코멘트 신고 모달을 열 수 있다', () => {
      cy.contains(momentsData[0].content).click();

      cy.get('[role="dialog"]').should('be.visible');

      cy.get('button[class*="ComplaintButton"], button[aria-label="신고"]').first().click();

      cy.contains('신고하기').should('be.visible');
      cy.contains('신고 사유를 선택해주세요').should('be.visible');
    });

    it('코멘트를 신고하면 해당 코멘트가 사라진다', () => {
      cy.contains(momentsData[0].content).click();

      cy.get('[role="dialog"]').should('be.visible');

      const firstComment = momentsData[0].comments?.[momentsData[0].comments.length - 1];

      if (firstComment) {
        cy.contains(firstComment.content).should('be.visible');

        cy.get('button[class*="ComplaintButton"], button[aria-label="신고"]').first().click();

        cy.get('[role="dialog"]')
          .last()
          .within(() => {
            cy.contains('스팸/광고').click();
            cy.get('button').contains('신고하기').click();
          });

        cy.wait('@reportComment');

        cy.contains('신고가 접수되었습니다').should('be.visible');

        cy.contains(firstComment.content).should('not.exist');
      }
    });
  });

  describe('시나리오 6: 코멘트가 없는 모멘트', () => {
    it('코멘트가 없는 모멘트는 클릭할 수 없다', () => {
      const momentWithoutComments = momentsData[2];

      cy.contains(momentWithoutComments.content)
        .closest('[class*="MyMomentsCard"]')
        .should('not.have.css', 'cursor', 'pointer');
    });

    it('코멘트가 없는 모멘트에는 코멘트 수가 표시되지 않는다', () => {
      const momentWithoutComments = momentsData[2];

      cy.contains(momentWithoutComments.content)
        .closest('[class*="MyMomentsCard"]')
        .within(() => {
          cy.get('svg[class*="Mail"]').should('not.exist');
        });
    });
  });

  describe('시나리오 7: 읽지 않은 알림', () => {
    it('읽지 않은 코멘트 알림이 있는 모멘트는 시각적으로 강조된다', () => {
      const unreadMoment = momentsData[0];

      cy.contains(unreadMoment.content)
        .closest('[class*="MyMomentsCard"]')
        .should('have.css', 'box-shadow')
        .and('not.equal', 'none');
    });

    it('모멘트를 클릭하면 알림이 읽음 처리된다', () => {
      const unreadMoment = momentsData[0];

      cy.intercept('PATCH', '**/api/v1/notifications/read-all', req => {
        expect(req.body.notificationIds).to.deep.equal(
          unreadMoment.momentNotification.notificationIds,
        );

        req.reply({
          statusCode: 200,
          body: {
            status: 200,
            data: { success: true },
          },
        });
      }).as('readNotifications');

      cy.contains(unreadMoment.content).click();

      cy.wait('@readNotifications');
    });
  });

  describe('시나리오 8: 무한 스크롤', () => {
    beforeEach(() => {
      mockGlobalAPIs();

      // 더 많은 데이터를 생성하여 스크롤이 발생하도록 함
      const firstPageData = Array.from({ length: 5 }, (_, i) => ({
        id: i + 1,
        momenterId: 1,
        content: `첫 페이지 모멘트 ${i + 1}`,
        createdAt: `2025-10-22T${10 + i}:00:00`,
        imageUrl: null,
        tagNames: ['테스트'],
        comments: null,
        momentNotification: {
          isRead: true,
          notificationIds: [],
        },
      }));

      const secondPageData = Array.from({ length: 3 }, (_, i) => ({
        id: i + 6,
        momenterId: 1,
        content: `두 번째 페이지 모멘트 ${i + 1}`,
        createdAt: `2025-10-22T${15 + i}:00:00`,
        imageUrl: null,
        tagNames: ['테스트'],
        comments: null,
        momentNotification: {
          isRead: true,
          notificationIds: [],
        },
      }));

      cy.intercept('GET', '**/api/v1/moments/me?*', req => {
        const url = new URL(req.url);
        const nextCursor = url.searchParams.get('nextCursor');

        if (!nextCursor) {
          req.reply({
            statusCode: 200,
            body: {
              status: 200,
              data: {
                items: firstPageData,
                nextCursor: 'cursor_page_2',
                hasNextPage: true,
                pageSize: 10,
              },
            },
          });
        } else {
          req.reply({
            statusCode: 200,
            body: {
              status: 200,
              data: {
                items: secondPageData,
                nextCursor: null,
                hasNextPage: false,
                pageSize: 10,
              },
            },
          });
        }
      }).as('getMomentsWithPagination');

      // 뷰포트를 작게 설정하여 스크롤이 확실히 생기도록
      cy.viewport(1280, 600);
      cy.visit('/collection/my-moment');
      cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getMomentsWithPagination']);
    });

    it('스크롤 시 추가 모멘트가 로드된다', () => {
      // 첫 페이지 데이터 확인
      cy.contains('첫 페이지 모멘트 1').should('be.visible');
      cy.contains('첫 페이지 모멘트 5').should('be.visible');

      cy.contains('두 번째 페이지 모멘트 1').should('not.exist');

      // 페이지를 여러 번 스크롤하여 확실하게 IntersectionObserver 트리거
      for (let i = 0; i < 5; i++) {
        cy.window().then(win => {
          win.scrollBy(0, 300);
        });
        cy.wait(200);
      }

      // 마지막으로 맨 아래까지 스크롤
      cy.window().then(win => {
        win.scrollTo(0, win.document.documentElement.scrollHeight);
      });

      // IntersectionObserver가 트리거되도록 충분한 시간 대기
      cy.wait(3000);

      // 두 번째 페이지 요청 대기
      cy.wait('@getMomentsWithPagination', { timeout: 15000 });

      cy.contains('두 번째 페이지 모멘트 1', { timeout: 10000 }).should('be.visible');
    });
  });

  describe('시나리오 9: 빈 상태', () => {
    beforeEach(() => {
      cy.intercept('GET', '**/api/v1/moments/me?*', {
        statusCode: 200,
        body: {
          status: 200,
          data: {
            items: [],
            nextCursor: null,
            hasNextPage: false,
            pageSize: 10,
          },
        },
      }).as('getEmptyMoments');

      cy.visit('/collection/my-moment');
      cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getEmptyMoments']);
    });

    it('모멘트가 없을 때 안내 메시지가 표시된다', () => {
      cy.contains('아직 모멘트가 없어요').should('be.visible');
      cy.contains('오늘의 모멘트를 작성하고 따뜻한 공감을 받아보세요').should('be.visible');
    });
  });

  describe('시나리오 10: 에러 상태', () => {
    beforeEach(() => {
      cy.intercept('GET', '**/api/v1/moments/me?*', {
        statusCode: 500,
        body: {
          status: 500,
          message: 'Internal Server Error',
        },
      }).as('getMomentsError');

      cy.visit('/collection/my-moment');
      cy.wait(['@checkLogin', '@getProfile', '@getNotifications', '@getMomentsError']);
    });

    it('에러 발생 시 에러 메시지가 표시된다', () => {
      cy.contains('문제가 발생했습니다').should('be.visible');
      cy.contains('일시적인 문제가 발생했습니다. 다시 시도해 주세요.').should('be.visible');
    });
  });
});
