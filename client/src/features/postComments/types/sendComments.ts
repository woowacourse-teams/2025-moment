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
