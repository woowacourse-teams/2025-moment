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
