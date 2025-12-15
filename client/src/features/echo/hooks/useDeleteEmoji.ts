import { useToast } from '@/shared/hooks';
import { useDeleteEmojiMutation } from '../api/useDeleteEmojiMutation';

export const useDeleteEmoji = () => {
  const { showError } = useToast();
  const { mutateAsync: deleteEmoji } = useDeleteEmojiMutation();

  const handleDeleteEmoji = async (emojiId: number) => {
    try {
      await deleteEmoji(emojiId);
    } catch {
      showError('이모지 삭제에 실패했습니다.', 3000);
    }
  };

  return {
    handleDeleteEmoji,
  };
};
