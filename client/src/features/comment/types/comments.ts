export interface CommentsResponse {
  status: number;
  data: [
    {
      id: number;
      content: string;
      createdAt: string;
      moment: {
        content: string;
        createdAt: string;
      };
      emojis: [
        {
          id: number;
          emojiType: string;
          userId: number;
        },
      ];
    },
  ];
}

export interface SendCommentsData {
  content: string;
  momentId: number;
}

export interface SendCommentsResponse {
  status: number;
  data: {
    commentId: number;
    content: string;
    createdAt: string;
  };
}

export interface SendCommentsError {
  code: string;
  message: string;
  status: number;
}

export type CommentCreationStatus = 'NOT_MATCHED' | 'ALREADY_COMMENTED' | 'WRITABLE';

export interface CommentCreationStatusResponse {
  status: number;
  data: {
    commentCreationStatus: CommentCreationStatus;
  };
}
