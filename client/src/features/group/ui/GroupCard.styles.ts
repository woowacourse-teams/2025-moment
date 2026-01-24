import styled from '@emotion/styled';

export const CardContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 12px;
  background: ${({ theme }) => theme.colors['slate-800']};
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    border-color: ${({ theme }) => theme.colors['yellow-500']};
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
`;

export const CardHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
`;

export const MenuButton = styled.button`
  padding: 4px;
  color: ${({ theme }) => theme.colors['gray-400']};
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background-color: ${({ theme }) => theme.colors['slate-700']};
    color: ${({ theme }) => theme.colors.white};
  }
`;

export const MenuDropdown = styled.div`
  position: absolute;
  top: 32px;
  right: 0;
  background: ${({ theme }) => theme.colors['slate-800']};
  border: 1px solid ${({ theme }) => theme.colors['slate-700']};
  border-radius: 8px;
  padding: 4px;
  min-width: 120px;
  z-index: 10;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
`;

export const MenuItem = styled.button`
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 0.875rem;
  color: ${({ theme }) => theme.colors['gray-200']};
  text-align: left;
  transition: background-color 0.2s;

  &:hover {
    background-color: ${({ theme }) => theme.colors['slate-700']};
  }

  &.danger {
    color: ${({ theme }) => theme.colors['red-500']};
    &:hover {
      background-color: ${({ theme }) => theme.colors['red-500']}1a; // 10% opacity
    }
  }
`;

export const GroupName = styled.h3`
  font-size: 18px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.white};
  margin: 0;
`;

export const OwnerBadge = styled.span`
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: ${({ theme }) => theme.colors['yellow-500']};
  color: black;
`;

export const Description = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
`;

export const CardFooter = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid ${({ theme }) => theme.colors['slate-700']};
`;

export const MemberCount = styled.span`
  font-size: 13px;
  color: ${({ theme }) => theme.colors['gray-600']};
`;

export const CreatedDate = styled.span`
  font-size: 12px;
  color: ${({ theme }) => theme.colors['gray-600']};
`;
