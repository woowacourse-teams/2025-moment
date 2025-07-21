import styled from '@emotion/styled';

export const PostCommentsPageContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin: 20px;
`;

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
  justify-content: space-between;
  gap: 8px;
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

export const ContentContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 10px;
`;

export const Emoji = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  background-color: ${({ theme }) => theme.colors['gray-600_20']};
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
`;
