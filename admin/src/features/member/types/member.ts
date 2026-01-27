// Member
export type MemberRole = "OWNER" | "MEMBER";

export interface Member {
  id: number;
  userId: number;
  nickname: string;
  role: MemberRole;
  joinedAt: string;
}

export interface MemberListData {
  content: Member[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// Pending member
export interface PendingMember {
  id: number;
  userId: number;
  nickname: string;
  requestedAt: string;
}

export interface PendingMemberListData {
  content: PendingMember[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
