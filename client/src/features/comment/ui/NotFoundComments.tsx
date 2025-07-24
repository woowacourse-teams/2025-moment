import styled from '@emotion/styled';
import { Eye } from 'lucide-react';

export const NotFoundComments = () => {
  return (
    <NotFoundCommentsContainer>
      <Eye />
      <span>아직 응답이 없어요.</span>
      <span>곧 누군가가 따뜻한 응답을 보내줄 거예요.</span>
    </NotFoundCommentsContainer>
  );
};

const NotFoundCommentsContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
