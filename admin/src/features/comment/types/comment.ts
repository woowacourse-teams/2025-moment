// Comment
export interface GroupComment {
  id: number;
  authorNickname: string;
  content: string;
  createdAt: string;
}

export interface GroupCommentListData {
  content: GroupComment[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
