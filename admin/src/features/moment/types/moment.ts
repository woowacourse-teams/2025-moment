// Moment
export interface GroupMoment {
  id: number;
  authorNickname: string;
  content: string;
  commentCount: number;
  createdAt: string;
}

export interface GroupMomentListData {
  content: GroupMoment[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
