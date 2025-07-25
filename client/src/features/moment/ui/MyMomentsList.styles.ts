import styled from '@emotion/styled';

export const TitleContainer = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const TitleWrapper = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
`;

export const TimeStamp = styled.span`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const Title = styled.span`
  font-size: ${({ theme }) => theme.typography.title.fontSize.small};
  font-weight: ${({ theme }) => theme.typography.fontWeight.large};
  color: ${({ theme }) => theme.colors.white};
`;

export const MomentsContainer = styled.section`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
`;

export const EmojiContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
`;
