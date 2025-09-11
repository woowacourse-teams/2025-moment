import styled from '@emotion/styled';

export const PostCommentsPageContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin: 20px;
`;

export const MyMomentContent = styled.p`
  font-size: 1.1rem;
  text-align: left;
  color: ${({ theme }) => theme.colors['gray-400']};
  word-break: break-all;
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

export const MyCommentsTagWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
`;

export const EchoContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
`;

export const NoEchoContent = styled.p`
  text-align: center;
  font-size: 1rem;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
