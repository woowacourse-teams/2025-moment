import styled from '@emotion/styled';

interface CardActionProps {
  children: React.ReactNode;
  position: 'center' | 'space-between';
}

export const CardAction = ({ children, position = 'center' }: CardActionProps) => {
  return <CardActionStyles position={position}>{children}</CardActionStyles>;
};

const CardActionStyles = styled.section<{ position: 'center' | 'space-between' }>`
  display: flex;
  justify-content: ${({ position }) => (position === 'center' ? 'center' : 'space-between')};
`;
