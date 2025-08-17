import styled from '@emotion/styled';

export const MyPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
`;

export const UserInfoSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const StatusSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const SettingSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const Email = styled.p`
  font-size: 120px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const UserInfo = styled.div`
  display: flex;
  gap: 16px;
`;
