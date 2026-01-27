// Group status enum
export type GroupStatus = "ACTIVE" | "DELETED";

// Group list item
export interface Group {
  id: number;
  name: string;
  description: string;
  ownerNickname: string;
  memberCount: number;
  momentCount: number;
  status: GroupStatus;
  createdAt: string;
  deletedAt: string | null;
}

// Group detail
export interface GroupDetail {
  id: number;
  name: string;
  description: string;
  ownerNickname: string;
  ownerId: number;
  memberCount: number;
  momentCount: number;
  status: GroupStatus;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

// Group list response (paginated)
export interface GroupListData {
  content: Group[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// Group stats for dashboard
export interface GroupStats {
  totalGroups: number;
  activeGroups: number;
  deletedGroups: number;
  totalMembers: number;
}

// Group list query params
export interface GroupListParams {
  page: number;
  size: number;
  keyword?: string;
  status?: GroupStatus | "";
}

// Update group request
export interface UpdateGroupRequest {
  name: string;
  description: string;
}
