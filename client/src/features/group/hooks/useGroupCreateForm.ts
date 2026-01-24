import { useState } from 'react';
import { useCreateGroupMutation } from '../api/useCreateGroupMutation';
import { useProfileQuery } from '@/features/auth/api/useProfileQuery';

const MAX_NAME_LENGTH = 50;
const MAX_DESCRIPTION_LENGTH = 200;

interface UseGroupCreateFormProps {
  onSuccess?: () => void;
}

export function useGroupCreateForm({ onSuccess }: UseGroupCreateFormProps) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const createGroupMutation = useCreateGroupMutation();
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
      onSuccess?.();
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
