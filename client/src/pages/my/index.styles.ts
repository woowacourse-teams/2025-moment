import styled from '@emotion/styled';

export const MyPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 40px;
`;

export const UserInfoSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
  width: 100%;
  max-width: 800px;
`;

export const RewardHistorySection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
  width: 100%;
  max-width: 800px;
`;

export const SettingSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  align-items: center;
  width: 100%;
  max-width: 800px;
`;

export const SectionTitle = styled.h2`
  font-size: 48px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors['white']};
  text-align: center;
  margin: 0;
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
  padding: 8px 0;
  border-bottom: 1px solid ${({ theme }) => theme.colors['gray-700']};
`;

export const UserBasicInfo = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
`;

export const Email = styled.h3`
  font-size: 32px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['white']};
  margin: 0;
`;

export const UserInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 16px;

  p {
    margin: 0;
    font-size: 18px;
    color: ${({ theme }) => theme.colors['gray-200']};

    &:first-of-type {
      font-weight: 600;
      color: ${({ theme }) => theme.colors['yellow-300']};
      font-size: 20px;
    }
  }
`;

export const LevelBadge = styled.div`
  background: transparent;
  color: ${({ theme }) => theme.colors['white']};
  padding: 6px 16px;
  border: 1px solid ${({ theme }) => theme.colors['gray-200']};
  border-radius: 20px;
  font-weight: 700;
  font-size: 14px;
  text-transform: uppercase;
`;

export const LevelIcon = styled.img`
  width: 48px;
  height: 48px;
`;

export const EXPSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
`;

export const EXPBarContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
`;

export const EXPStats = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-200']};
  min-width: 120px;

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
  margin: 0 0 8px 0;
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
