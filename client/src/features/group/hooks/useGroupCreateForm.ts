import { useState } from 'react';
import { useCreateGroupMutation } from '../api/useCreateGroupMutation';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { useGroupsQuery } from '../api/useGroupsQuery';
import { useCreateInviteMutation } from '../api/useCreateInviteMutation';
import { api } from '@/app/lib/api';

const MAX_NAME_LENGTH = 50;
const MAX_DESCRIPTION_LENGTH = 200;

interface UseGroupCreateFormProps {
  onSuccess?: (groupId: number, code: string) => void;
}

export function useGroupCreateForm({ onSuccess }: UseGroupCreateFormProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const createGroupMutation = useCreateGroupMutation();
  const { refetch: refetchGroups } = useGroupsQuery({ enabled: false });
  const { data: profile } = useProfileQuery({ enabled: true });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim() || !description.trim() || !profile?.nickname) return;

    try {
      await createGroupMutation.mutateAsync({
        name: name.trim(),
        description: description.trim(),
        ownerNickname: profile.nickname,
      });

      // Fetch groups to get the new group ID (workaround)
      const { data: groupsResponse } = await refetchGroups();
      const groups = groupsResponse?.data || [];

      // Assume the group with largest ID is the one we just created
      const newestGroup = groups.sort((a, b) => b.groupId - a.groupId)[0];

      if (newestGroup) {
        // Create invite link directly
        const inviteResponse = await api.post(`/groups/${newestGroup.groupId}/invite`);
        const code = inviteResponse.data.data;

        if (onSuccess) {
          onSuccess(newestGroup.groupId, code);
        }
      }
    } catch (error) {
      console.error('Failed to create group:', error);
    }
  };

  const isValid =
    name.trim().length > 0 &&
    name.length <= MAX_NAME_LENGTH &&
    description.trim().length > 0 &&
    description.length <= MAX_DESCRIPTION_LENGTH &&
    !!profile?.nickname;

  return {
    name,
    description,
    profile,

    setName,
    setDescription,

    isValid,
    isPending: createGroupMutation.isPending,

    MAX_NAME_LENGTH,
    MAX_DESCRIPTION_LENGTH,

    handleSubmit,
  };
}
