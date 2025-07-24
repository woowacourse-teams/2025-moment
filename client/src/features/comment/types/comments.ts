export interface postCommentsResponse {
  status: number;
  data: [
    {
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

export interface sendCommentsData {
  content: string;
  momentId: number;
}

export interface sendCommentsResponse {
  status: number;
  data: {
    commentId: number;
    content: string;
    createdAt: string;
  };
}

export interface sendCommentsError {
  code: string;
  message: string;
  status: number;
}
