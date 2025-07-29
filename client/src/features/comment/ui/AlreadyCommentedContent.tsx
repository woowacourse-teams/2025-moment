import { Card } from '@/shared/ui/card/Card';
import styled from '@emotion/styled';
import { CheckCircleIcon } from 'lucide-react';

export function AlreadyCommentedContent() {
  return (
    <Card width="medium">
      <AlreadyCommentedContentWrapper>
        <AlreadyCommentedContentIcon size={24} />
        <AlreadyCommentedContentContainer>
          <AlreadyCommentedContentTitle>이미 코멘트를 남겼어요</AlreadyCommentedContentTitle>
          <AlreadyCommentedContentSubtitle>
            내일 다시 코멘트를 작성할 수 있어요!
          </AlreadyCommentedContentSubtitle>
        </AlreadyCommentedContentContainer>
      </AlreadyCommentedContentWrapper>
    </Card>
  );
}

const AlreadyCommentedContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  height: 55vh;
  align-items: center;
  justify-content: center;
  gap: 30px;
`;

const AlreadyCommentedContentIcon = styled(CheckCircleIcon)`
  width: 30px;
  height: 30px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

const AlreadyCommentedContentContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
`;

const AlreadyCommentedContentTitle = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

const AlreadyCommentedContentSubtitle = styled.div`
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
