import { CommonSkeletonCard } from './CommonSkeletonCard';
import { DeferredComponent } from '@/shared/design-system/skeleton';
import styled from '@emotion/styled';

interface SuspenseSkeletonProps {
  variant?: 'moment' | 'comment' | 'rewardHistory';
  count?: number;
}

const SkeletonContainer = styled.section`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  margin: 20px;
`;

export const SuspenseSkeleton = ({ variant = 'moment', count = 3 }: SuspenseSkeletonProps) => {
  return (
    <SkeletonContainer>
      <DeferredComponent>
        {Array.from({ length: count }).map((_, index) => (
          <CommonSkeletonCard key={`skeleton-${variant}-${index}`} variant={variant} />
        ))}
      </DeferredComponent>
    </SkeletonContainer>
  );
};
