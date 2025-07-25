import styled from '@emotion/styled';

interface Emoji {
  children: React.ReactNode;
  onClick: () => void;
}

export const Emoji = ({ children, onClick }: Emoji) => {
  return <EmojiStyle onClick={onClick}>{children}</EmojiStyle>;
};

export const EmojiStyle = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  background-color: ${({ theme }) => theme.colors['gray-600_20']};
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
`;
