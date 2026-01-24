import { useParams } from 'react-router';

export function useCurrentGroup() {
  const { groupId: groupIdParam } = useParams<{ groupId: string }>();
  const currentGroupId = groupIdParam ? Number(groupIdParam) : null;

  const getCurrentGroupId = (): number | null => {
    return currentGroupId;
  };

  return {
    currentGroupId,
    getCurrentGroupId,
  };
}
