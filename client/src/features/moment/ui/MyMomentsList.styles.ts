import styled from '@emotion/styled';

export const TitleContainer = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
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
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;

  ${({ theme }) => theme.mediaQueries.tablet} {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  ${({ theme }) => theme.mediaQueries.mobile} {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 12px;
  }
`;

export const EmojiContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
`;

export const Content = styled.p`
  height: 100%;
`;

export const EmpathyContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;
