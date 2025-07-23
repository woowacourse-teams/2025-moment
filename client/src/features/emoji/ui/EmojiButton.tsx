import { CustomTheme } from '@/app/styles/theme';
import { useEmojiMutation } from '@/features/emoji/hooks/useEmojiMutation';
import { Button } from '@/shared/ui';

interface EmojiButtonProps {
  commentId?: number;
  emojiType?: 'HEART' | 'LIKE' | 'SMILE';
}

// 현재는 commentId와 emojiType을 옵셔널로 주지만 나중엔 필수로 바꿔야함
export const EmojiButton = ({ commentId = 1, emojiType = 'HEART' }: EmojiButtonProps) => {
  const { mutateAsync: sendEmoji } = useEmojiMutation();

  const handleClick = async () => {
    try {
      await sendEmoji({
        emojiType,
        commentId,
      });
    } catch {
      console.error('스티커 보내기 실패');
    }
  };

  return <Button externalVariant={buttonVariant} title={'+ 스티커 보내기'} onClick={handleClick} />;
};

const buttonVariant = (theme: CustomTheme) => `
  border: 1px solid ${theme.colors['yellow-500']};
  color: ${theme.colors['yellow-500']};
  height: 20px;
  border-radius: 25px;
  padding: 0 16px;
  font-size: 12px;
  font-weight: bold;
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  &:hover:not(:disabled) {
    background-color: ${theme.colors['yellow-300_10']};
  }
`;
