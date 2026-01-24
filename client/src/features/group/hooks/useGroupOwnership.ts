import { useCurrentGroup } from './useCurrentGroup';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { useGroupDetailQuery } from '@/features/group/api/useGroupDetailQuery';

export function useGroupOwnership(groupId?: number | string) {
  const { currentGroupId } = useCurrentGroup();
  const { data: profile } = useProfileQuery({ enabled: true });

  const targetGroupId = groupId ?? currentGroupId;
  const { data: groupData } = useGroupDetailQuery(targetGroupId?.toString() || '');
  const group = groupData?.data;

  if (!targetGroupId || !profile || !group) {
    return { isOwner: false, ownerId: null };
  }

  const isOwner = group.ownerId === profile.id;

  return {
    isOwner,
    ownerId: group.ownerId,
  };
}
