import { Skeleton, SkeletonText } from '@/shared/design-system/skeleton';
import { theme } from '@/shared/styles/theme';
import styled from '@emotion/styled';
import React from 'react';

const Wrapper = styled.main`
  width: 100%;
  max-width: 800px;
  min-height: 100vh;
  margin: 0 auto;
  padding: 40px 24px;
  background-color: ${theme.colors['navy-900']};

  @media (max-width: 768px) {
    padding: 24px 16px;
  }
`;

const TitleWrapper = styled.div`
  margin-bottom: 32px;
`;

const Section = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 28px;
`;

export const StaticPageSkeleton: React.FC = () => {
  return (
    <Wrapper>
      <TitleWrapper>
        <Skeleton width="180px" height="32px" borderRadius="6px" />
      </TitleWrapper>
      {Array.from({ length: 4 }, (_, i) => `section-${i}`).map(key => (
        <Section key={key}>
          <Skeleton width="140px" height="20px" borderRadius="4px" />
          <SkeletonText lines={4} lineHeight="18px" gap="8px" />
        </Section>
      ))}
    </Wrapper>
  );
};
