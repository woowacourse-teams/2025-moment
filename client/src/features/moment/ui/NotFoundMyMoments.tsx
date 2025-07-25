import styled from '@emotion/styled';
import { Eye } from 'lucide-react';

export const NotFoundMyMoments = () => {
  return (
    <NotFoundMyMomentsContainer>
      <Eye />
      <span>아직 작성한 모멘트가 없어요</span>
      <span>오늘 하루 인상깊었던 부분을 사람들과 공유해봐요.</span>
    </NotFoundMyMomentsContainer>
  );
};

const NotFoundMyMomentsContainer = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
