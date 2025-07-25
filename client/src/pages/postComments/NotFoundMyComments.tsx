import styled from '@emotion/styled';
import { Eye } from 'lucide-react';

export const NotFoundMyComments = () => {
  return (
    <NotFoundMyCommentsContainer>
      <Eye />
      <span>아직 보낸 코멘트가 없어요</span>
      <span>누군가에게 따뜻한 코멘트를 보내주세요.</span>
    </NotFoundMyCommentsContainer>
  );
};

const NotFoundMyCommentsContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
