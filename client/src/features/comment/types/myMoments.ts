export interface MyMomentsResponse {
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
