import { useCurrentGroup } from './useCurrentGroup';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';

export function useGroupOwnership(groupId?: number | string) {
  const { currentGroup } = useCurrentGroup();
  const { data: profile } = useProfileQuery();

  const targetGroupId = groupId ?? currentGroup?.id;

  if (!targetGroupId || !profile || !currentGroup) {
    return { isOwner: false, ownerId: null };
  }

  const isOwner = currentGroup.ownerId === profile.data.id;

  return {
    isOwner,
    ownerId: currentGroup.ownerId,
  };
}
