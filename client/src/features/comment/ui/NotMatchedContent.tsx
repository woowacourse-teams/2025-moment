import { Card } from '@/shared/ui';
import styled from '@emotion/styled';
import { SearchIcon } from 'lucide-react';

export function NotMatchedContent() {
  return (
    <Card width="medium">
      <NotMatchedContentWrapper>
        <NotMatchedContentIcon size={24} />
        <NotMatchedContentContainer>
          <NotMatchedContentTitle>아직 매칭되지 않았어요</NotMatchedContentTitle>
          <NotMatchedContentSubtitle>
            다른 사용자와 매칭되면 코멘트를 달 수 있어요
          </NotMatchedContentSubtitle>
        </NotMatchedContentContainer>
      </NotMatchedContentWrapper>
    </Card>
  );
}

const NotMatchedContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  height: 55vh;
  align-items: center;
  justify-content: center;
  gap: 30px;
`;

const NotMatchedContentIcon = styled(SearchIcon)`
  width: 30px;
  height: 30px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

const NotMatchedContentContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
`;

const NotMatchedContentTitle = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

const NotMatchedContentSubtitle = styled.div`
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
