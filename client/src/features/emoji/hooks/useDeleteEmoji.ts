import { useDeleteEmojiMutation } from './useDeleteEmojiMutation';

export const useDeleteEmoji = () => {
  const { mutateAsync: deleteEmoji } = useDeleteEmojiMutation();

  const handleDeleteEmoji = async (emojiId: number) => {
    try {
      await deleteEmoji(emojiId);
    } catch {
      alert('이모지 삭제에 실패했습니다.');
    }
  };

  return {
    handleDeleteEmoji,
  };
};
