import { Skeleton, SkeletonText } from '@/shared/design-system/skeleton';
import React from 'react';
import * as S from './CommonSkeletonCard.styles';

interface CommonSkeletonCardProps {
  variant?: 'moment' | 'comment' | 'rewardHistory';
}

export const CommonSkeletonCard: React.FC<CommonSkeletonCardProps> = ({ variant = 'moment' }) => {
  return (
    <S.SkeletonCard variant={variant}>
      <S.SkeletonCardTitle>
        <S.SkeletonTitleRow>
          <Skeleton width="16px" height="16px" borderRadius="50%" />
          <Skeleton width="120px" height="16px" />
        </S.SkeletonTitleRow>
        <SkeletonText lines={2} lineHeight="18px" />
      </S.SkeletonCardTitle>

      {variant === 'moment' && (
        <>
          <S.SkeletonMomentContent>
            <SkeletonText lines={3} lineHeight="18px" />
          </S.SkeletonMomentContent>
          <S.SkeletonMomentBottom>
            <S.SkeletonImageArea>
              <Skeleton width="80px" height="80px" borderRadius="6px" />
            </S.SkeletonImageArea>
            <S.SkeletonTagArea>
              <Skeleton width="60px" height="24px" borderRadius="12px" />
              <Skeleton width="70px" height="24px" borderRadius="12px" />
            </S.SkeletonTagArea>
          </S.SkeletonMomentBottom>
        </>
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

          <S.SkeletonCardAction>
            <S.SkeletonActionButtons>
              <Skeleton width="40px" height="32px" borderRadius="20px" />
              <Skeleton width="40px" height="32px" borderRadius="20px" />
              <Skeleton width="40px" height="32px" borderRadius="20px" />
            </S.SkeletonActionButtons>
          </S.SkeletonCardAction>
        </>
      )}

      {variant === 'rewardHistory' && (
        <S.SkeletonRewardHistoryTable>
          <thead>
            <tr>
              <th>
                <Skeleton width="100px" height="16px" />
              </th>
              <th>
                <Skeleton width="100px" height="16px" />
              </th>
            </tr>
          </thead>
          <tbody>
            {Array.from({ length: 10 }, (_, index) => `skeleton-row-${index}`).map(uniqueKey => (
              <tr key={uniqueKey}>
                <td>
                  <Skeleton width="100px" height="16px" />
                </td>
              </tr>
            ))}
          </tbody>
        </S.SkeletonRewardHistoryTable>
      )}
    </S.SkeletonCard>
  );
};
