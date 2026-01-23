import { useGroupMembersQuery } from '../api/useGroupMembersQuery';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';

export function useGroupMembership(groupId: number | string) {
  const { data: members } = useGroupMembersQuery(groupId);
  const { data: profile } = useProfileQuery();

  if (!members || !profile) {
    return {
      isMember: false,
      memberInfo: null,
    };
  }

  const memberInfo = members.data.find(member => member.userId === profile.data.id);

  return {
    isMember: !!memberInfo,
    memberInfo: memberInfo ?? null,
  };
}
