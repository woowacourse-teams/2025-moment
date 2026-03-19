import styled from '@emotion/styled';

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
`;

export const Title = styled.h3`
  font-size: 1.125rem;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-400']};
`;

export const List = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const UserItem = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: ${({ theme }) => theme.colors['gray-200_40']};
  border-radius: 8px;
`;

export const Nickname = styled.span`
  font-size: 0.9375rem;
  font-weight: 500;
  color: ${({ theme }) => theme.colors['white']};
`;

export const EmptyState = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background-color: ${({ theme }) => theme.colors['gray-200_40']};
  border-radius: 12px;
  color: ${({ theme }) => theme.colors['gray-600']};
  font-size: 0.9375rem;
`;
