export interface sendCommentsData {
  comment: string;
  momentId: string;
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
