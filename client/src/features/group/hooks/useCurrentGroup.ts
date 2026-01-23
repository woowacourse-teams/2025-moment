import { useGroupContext } from '../context/GroupContext';
import { Group } from '../types/group';

export function useCurrentGroup() {
  const { currentGroup, setCurrentGroup, clearCurrentGroup } = useGroupContext();

  const selectGroup = (group: Group) => {
    setCurrentGroup(group);
  };

  const getCurrentGroupId = (): number | null => {
    return currentGroup?.id ?? null;
  };

  return {
    currentGroup,
    selectGroup,
    clearCurrentGroup,
    getCurrentGroupId,
  };
}
