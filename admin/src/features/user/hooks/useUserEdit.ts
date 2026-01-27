import { useState, useEffect } from "react";
import { useUpdateUserMutation } from "../api/useUpdateUserMutation";

const MAX_NICKNAME_LENGTH = 20;

interface UseUserEditProps {
  userId: string;
  initialNickname: string;
  onSuccess?: () => void;
}

export function useUserEdit({
  userId,
  initialNickname,
  onSuccess,
}: UseUserEditProps) {
  const [nickname, setNickname] = useState(initialNickname);
  const updateUserMutation = useUpdateUserMutation(userId);

  useEffect(() => {
    setNickname(initialNickname);
  }, [initialNickname]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isValid) return;

    await updateUserMutation.mutateAsync({ nickname: nickname.trim() });
    onSuccess?.();
  };

  const isValid =
    nickname.trim().length > 0 &&
    nickname.length <= MAX_NICKNAME_LENGTH &&
    nickname.trim() !== initialNickname;

  return {
    nickname,
    setNickname,
    isValid,
    isPending: updateUserMutation.isPending,
    handleSubmit,
    MAX_NICKNAME_LENGTH,
  };
}
