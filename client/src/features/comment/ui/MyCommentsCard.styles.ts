import styled from '@emotion/styled';

export const PostCommentsPageContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin: 20px;
`;

export const MyMomentContent = styled.p`
  font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
  ${({ theme }) => theme.mediaQueries.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
  }
  text-align: left;
  color: ${({ theme }) => theme.colors['gray-400']};
  word-break: break-all;
`;

export const DeletedMomentText = styled.p`
  font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
  ${({ theme }) => theme.mediaQueries.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
  }
  color: ${({ theme }) => theme.colors['gray-400']};
  font-style: italic;
  text-align: left;
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
  font-size: ${({ theme }) => theme.typography.fontSize.title.small};
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

export const MomentContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 10px;
  margin-bottom: 10px;
  text-align: left;
  padding: 0 16px;
`;

export const MyCommentsContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 10px;
  margin-bottom: 10px;
  text-align: left;
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
  font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
  ${({ theme }) => theme.mediaQueries.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
  }
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const CommentImageContainer = styled.div`
  display: flex;
  justify-content: left;
  margin-top: 8px;
`;

export const CommentImage = styled.img`
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.05);
  }
`;

export const ImageOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  cursor: pointer;
`;

export const FullscreenImage = styled.img`
  max-width: 80vw;
  max-height: 80vh;
  object-fit: contain;
`;

export const CommentContent = styled.p`
  font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
  ${({ theme }) => theme.mediaQueries.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
  }
  text-align: left;
  word-break: break-all;
`;

export const SubTitle = styled.p`
  font-size: ${({ theme }) => theme.typography.fontSize.subTitle.medium};
  ${({ theme }) => theme.mediaQueries.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileSubTitle.medium};
  }
  text-align: left;
  word-break: break-all;
`;
