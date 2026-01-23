export interface Group {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  createdAt: string;
  memberCount: number;
}

export interface GroupMember {
  id: number;
  userId: number;
  nickname: string;
  role: 'OWNER' | 'MEMBER';
  joinedAt: string;
  profileImage?: string | null;
  level?: string;
}

export interface PendingMember {
  id: number;
  userId: number;
  nickname: string;
  requestedAt: string;
  profileImage?: string | null;
}

export interface GroupInvite {
  code: string;
  groupId: number;
  groupName: string;
  expiresAt?: string;
}

export interface InviteInfo {
  groupId: number;
  groupName: string;
  description: string;
  memberCount: number;
  isValid: boolean;
}

// API Request Types
export interface CreateGroupRequest {
  name: string;
  description: string;
  ownerNickname: string;
}

export interface UpdateGroupRequest {
  name?: string;
  description?: string;
}

export interface UpdateProfileRequest {
  nickname?: string;
  profileImage?: string;
}

export interface JoinGroupRequest {
  code: string;
}

// API Response Types
export interface GroupsResponse {
  status: number;
  data: Group[];
}

export interface GroupDetailResponse {
  status: number;
  data: Group;
}

export interface GroupMembersResponse {
  status: number;
  data: GroupMember[];
}

export interface PendingMembersResponse {
  status: number;
  data: PendingMember[];
}

export interface InviteResponse {
  status: number;
  data: {
    code: string;
    expiresAt?: string;
  };
}

export interface InviteInfoResponse {
  status: number;
  data: InviteInfo;
}

export interface GroupActionResponse {
  status: number;
  data: string;
}
