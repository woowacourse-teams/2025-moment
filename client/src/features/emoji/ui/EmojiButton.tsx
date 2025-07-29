import { CustomTheme } from '@/app/styles/theme';
import { Button } from '@/shared/ui';
import { useEmojiMutation } from '../hooks/useEmojiMutation';

interface EmojiButtonProps {
  commentId: number;
  emojiType?: string;
}

// 현재는 emojiType을 옵셔널로 주지만 나중엔 필수로 바꿔야함
export const EmojiButton = ({ commentId, emojiType = 'HEART' }: EmojiButtonProps) => {
  const { mutateAsync: sendEmoji } = useEmojiMutation();
  const handleClick = async () => {
    try {
      await sendEmoji({ emojiType, commentId });
    } catch {
      alert('스티커 보내기 실패');
    }
  };

  return <Button externalVariant={buttonVariant} title={'+ 스티커 보내기'} onClick={handleClick} />;
};

const buttonVariant = (theme: CustomTheme) => `
  border: 1px solid ${theme.colors['yellow-500']};
  color: ${theme.colors['yellow-500']};
  border-radius: 25px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: bold;
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  &:hover:not(:disabled) {
    background-color: ${theme.colors['yellow-300_10']};
  }
`;
