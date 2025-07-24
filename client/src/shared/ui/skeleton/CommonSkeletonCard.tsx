import { Skeleton, SkeletonText } from '@/shared/ui/skeleton/Skeleton';
import React from 'react';
import * as S from './CommonSkeletonCard.styles';

interface CommonSkeletonCardProps {
  variant?: 'moment' | 'comment';
}

export const CommonSkeletonCard: React.FC<CommonSkeletonCardProps> = ({ variant = 'moment' }) => {
  return (
    <S.SkeletonCard>
      <S.SkeletonCardTitle>
        <S.SkeletonTitleRow>
          <Skeleton width="16px" height="16px" borderRadius="50%" />
          <Skeleton width="120px" height="16px" />
        </S.SkeletonTitleRow>
        <SkeletonText lines={2} lineHeight="18px" />
      </S.SkeletonCardTitle>

      {variant === 'moment' && (
        <S.SkeletonCardContent>
          <S.SkeletonContentHeader>
            <Skeleton width="20px" height="20px" borderRadius="50%" />
            <Skeleton width="100px" height="16px" />
          </S.SkeletonContentHeader>
          <S.SkeletonSimpleCard>
            <SkeletonText lines={1} lineHeight="16px" />
          </S.SkeletonSimpleCard>
        </S.SkeletonCardContent>
      )}

      {variant === 'comment' && (
        <>
          <S.SkeletonSection>
            <S.SkeletonSectionHeader>
              <Skeleton width="20px" height="20px" borderRadius="50%" />
              <Skeleton width="100px" height="16px" />
            </S.SkeletonSectionHeader>
            <S.SkeletonSimpleCard>
              <SkeletonText lines={2} lineHeight="16px" />
            </S.SkeletonSimpleCard>
          </S.SkeletonSection>

          <S.SkeletonSection>
            <S.SkeletonSectionHeader>
              <Skeleton width="20px" height="20px" borderRadius="50%" />
              <Skeleton width="120px" height="16px" />
            </S.SkeletonSectionHeader>
            <S.SkeletonYellowCard>
              <SkeletonText lines={1} lineHeight="16px" />
            </S.SkeletonYellowCard>
          </S.SkeletonSection>

          <S.SkeletonSection>
            <S.SkeletonSectionHeader>
              <Skeleton width="20px" height="20px" borderRadius="50%" />
              <Skeleton width="100px" height="16px" />
            </S.SkeletonSectionHeader>
            <S.SkeletonEmojiContainer>
              <Skeleton width="32px" height="32px" borderRadius="50%" />
              <Skeleton width="32px" height="32px" borderRadius="50%" />
              <Skeleton width="32px" height="32px" borderRadius="50%" />
            </S.SkeletonEmojiContainer>
          </S.SkeletonSection>
        </>
      )}

      {variant === 'moment' && (
        <S.SkeletonCardAction>
          <S.SkeletonActionButtons>
            <Skeleton width="40px" height="32px" borderRadius="20px" />
            <Skeleton width="40px" height="32px" borderRadius="20px" />
            <Skeleton width="40px" height="32px" borderRadius="20px" />
          </S.SkeletonActionButtons>
        </S.SkeletonCardAction>
      )}
    </S.SkeletonCard>
  );
};
