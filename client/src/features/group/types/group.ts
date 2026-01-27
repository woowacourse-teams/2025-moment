export interface Group {
  groupId: number;
  name: string;
  description: string;
  memberId: number;
  myNickname: string;
  isOwner: boolean;
  memberCount: number;
  createdAt?: string;
  ownerId?: number;
}

export interface GroupMember {
  memberId: number;
  userId: number;
  nickname: string;
  role: 'OWNER' | 'MEMBER';
  joinedAt: string;
  profileImage?: string | null;
  level?: string;
}

export interface PendingMember {
  memberId: number;
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
  inviteCode: string;
  nickname: string;
}

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
  data: string;
}

export interface InviteInfoResponse {
  status: number;
  data: InviteInfo;
}

export interface GroupActionResponse {
  status: number;
  data: string;
}
