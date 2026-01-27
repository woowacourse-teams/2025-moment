import { useState } from 'react';
import { useGroupMembersQuery } from '../api/useGroupMembersQuery';
import { useGroupPendingMembersQuery } from '../api/useGroupPendingMembersQuery';
import { useApproveMemberMutation } from '../api/useApproveMemberMutation';
import { useRejectMemberMutation } from '../api/useRejectMemberMutation';
import { useKickMemberMutation } from '../api/useKickMemberMutation';
import { useTransferOwnershipMutation } from '../api/useTransferOwnershipMutation';

const DEFAULT_PAGE_SIZE = 20;

export function useGroupMembers(groupId: string) {
  const [memberPage, setMemberPage] = useState(0);
  const [pendingPage, setPendingPage] = useState(0);

  const membersQuery = useGroupMembersQuery({
    groupId,
    page: memberPage,
    size: DEFAULT_PAGE_SIZE,
  });

  const pendingQuery = useGroupPendingMembersQuery({
    groupId,
    page: pendingPage,
    size: DEFAULT_PAGE_SIZE,
  });

  const approveMutation = useApproveMemberMutation(groupId);
  const rejectMutation = useRejectMemberMutation(groupId);
  const kickMutation = useKickMemberMutation(groupId);
  const transferMutation = useTransferOwnershipMutation(groupId);

  return {
    members: membersQuery.data?.content ?? [],
    membersTotalPages: membersQuery.data?.totalPages ?? 0,
    memberPage,
    setMemberPage,
    isMembersLoading: membersQuery.isLoading,

    pendingMembers: pendingQuery.data?.content ?? [],
    pendingTotalPages: pendingQuery.data?.totalPages ?? 0,
    pendingPage,
    setPendingPage,
    isPendingLoading: pendingQuery.isLoading,

    approveMember: (memberId: number) => approveMutation.mutateAsync(memberId),
    rejectMember: (memberId: number) => rejectMutation.mutateAsync(memberId),
    kickMember: (memberId: number) => kickMutation.mutateAsync(memberId),
    transferOwnership: (memberId: number) => transferMutation.mutateAsync(memberId),
    isApproving: approveMutation.isPending,
    isRejecting: rejectMutation.isPending,
    isKicking: kickMutation.isPending,
    isTransferring: transferMutation.isPending,
  };
}
