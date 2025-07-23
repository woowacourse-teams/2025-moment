import styled from '@emotion/styled';

interface CardContentProps {
  children: React.ReactNode;
}

export const CardContent = ({ children }: CardContentProps) => {
  return <CardContentStyles>{children}</CardContentStyles>;
};

const CardContentStyles = styled.section`
  display: flex;
  flex-direction: column;
  text-align: center;
  gap: 5px;
  margin: 10px 0;
`;
