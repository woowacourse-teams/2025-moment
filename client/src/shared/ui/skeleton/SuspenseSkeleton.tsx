import { CommonSkeletonCard } from './CommonSkeletonCard';
import { DeferredComponent } from '@/shared/design-system/skeleton';
import styled from '@emotion/styled';

interface SuspenseSkeletonProps {
  variant?: 'moment' | 'comment' | 'rewardHistory';
  count?: number;
}

const SkeletonContainer = styled.section`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
  }

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 0 12px;
  }
`;

export const SuspenseSkeleton = ({ variant = 'moment', count = 3 }: SuspenseSkeletonProps) => {
  return (
    <DeferredComponent>
      <SkeletonContainer>
        {Array.from({ length: count }).map((_, index) => (
          <CommonSkeletonCard key={`skeleton-${variant}-${index}`} variant={variant} />
        ))}
      </SkeletonContainer>
    </DeferredComponent>
  );
};
