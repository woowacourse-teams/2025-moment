import styled from '@emotion/styled';

export const MyPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-top: 10vh;
  align-items: center;
  gap: 40px;

  @media (max-width: 768px) {
    margin-top: 5vh;
    gap: 24px;
    padding: 0 16px;
  }
  @media (max-width: 1024px) {
    gap: 32px;
    padding: 0 24px;
  }
`;

export const UserInfoSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  width: 100%;
  max-width: 800px;

  @media (max-width: 768px) {
    max-width: 100%;
  }
`;

export const RewardHistorySection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
  width: 100%;
  max-width: 800px;

  @media (max-width: 768px) {
    max-width: 100%;
  }
`;

export const SettingSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
  width: 100%;
  max-width: 800px;
`;

export const SectionTitleContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
  width: 90%;
  max-width: 800px;
`;

export const SectionTitle = styled.p`
  font-size: 36px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors['white']};
  margin: 0;

  @media (max-width: 768px) {
    font-size: 24px;
  }
  @media (max-width: 1024px) {
    font-size: 30px;
  }
`;

export const ButtonContainer = styled.div`
  display: flex;
  gap: 8px;

  @media (max-width: 768px) {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }

  @media (max-width: 480px) {
    button {
      padding: 8px 12px;
      font-size: 12px;
    }
  }
`;

export const UserInfoContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
`;

export const RewardHistoryContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
`;

export const UserProfileSection = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid ${({ theme }) => theme.colors['gray-700']};

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
`;

export const Email = styled.h3`
  font-size: 32px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['white']};
  margin: 0;

  @media (max-width: 768px) {
    font-size: 20px;
  }

  @media (max-width: 1024px) {
    font-size: 24px;
  }
`;

export const UserInfo = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  @media (max-width: 768px) {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  p {
    margin: 0;
    font-size: 18px;
    color: ${({ theme }) => theme.colors['gray-200']};

    &:first-of-type {
      font-weight: 600;
      color: ${({ theme }) => theme.colors['yellow-300']};
      font-size: 20px;

      @media (max-width: 768px) {
        font-size: 16px;
      }
    }

    @media (max-width: 768px) {
      font-size: 14px;
    }
  }
`;

export const LevelBadge = styled.div`
  background: transparent;
  color: ${({ theme }) => theme.colors['white']};
  padding: 2px 8px;
  font-weight: 700;
  font-size: 18px;
  text-transform: uppercase;

  @media (max-width: 768px) {
    font-size: 14px;
    padding: 2px 6px;
  }
`;

export const LevelIcon = styled.img`
  width: 48px;
  height: 48px;

  @media (max-width: 768px) {
    width: 36px;
    height: 36px;
  }
`;

export const EXPSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 0;
`;

export const EXPContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
`;

export const EXPStatsContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid ${({ theme }) => theme.colors['gray-200']};
  border-radius: 10px;
  padding: 4px 12px;

  @media (max-width: 768px) {
    gap: 8px;
    padding: 8px;
    flex-wrap: wrap;
    justify-content: center;
  }
`;

export const LevelButtonContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const LevelWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-200']};
`;

export const EXPStats = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-200']};

  @media (max-width: 768px) {
    font-size: 14px;
    gap: 4px;
  }

  .current {
    color: ${({ theme }) => theme.colors['yellow-300']};
  }

  .separator {
    color: ${({ theme }) => theme.colors['gray-200']};
  }

  .total {
    color: ${({ theme }) => theme.colors['gray-400']};
  }
`;

export const EXPLabel = styled.p`
  font-size: 14px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors['gray-400']};
  justify-content: center;
  margin: 0;
`;

export const Divider = styled.hr`
  border: none;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    ${({ theme }) => theme.colors['gray-700']},
    transparent
  );
  margin: 32px 0;
  width: 100%;
`;

export const SettingLink = styled.p`
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['white']};

  cursor: pointer;

  &:hover {
    color: ${({ theme }) => theme.colors['yellow-300']};
  }
`;
