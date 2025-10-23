import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const TitleWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
`;

export const UserInfoWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const ActionWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
`;

export const LevelImage = styled.img`
  width: 30px;
  height: 30px;
  object-fit: contain;
`;

export const ComplaintButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  cursor: pointer;
  background-color: transparent;
  ${({ theme }) => css`
    color: ${theme.colors['red-500']};
  `}
`;

export const RefreshButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  border: none;
  border-radius: 6px;
  background-color: transparent;
  ${({ theme }) => css`
    color: ${theme.colors['yellow-500']};
    &:hover {
      background-color: ${theme.colors['yellow-300_10']};
    }
  `}
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(1.05);
  }
`;

export const NotLoggedIcon = styled.img`
  width: 30px;
  height: 30px;
  object-fit: contain;
`;

export const NotLoggedNickname = styled.p`
  font-size: 16px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors['yellow-500']};
`;

export const MyCommentsContentWrapper = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 10px;
  text-align: left;
`;

export const MomentImageContainer = styled.div`
  display: flex;
  margin: 8px 0;
`;

export const MomentImage = styled.img`
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

export const MomentContent = styled.p`
  font-size: ${({ theme }) => theme.typography.fontSize.content.medium};
  ${({ theme }) => theme.mediaQueries.mobile} {
    font-size: ${({ theme }) => theme.typography.fontSize.mobileContent.medium};
  }
  text-align: left;
  word-break: break-all;
`;

export const CommentSection = styled.section`
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
`;
