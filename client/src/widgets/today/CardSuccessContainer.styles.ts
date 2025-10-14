import styled from '@emotion/styled';

export const CardSuccessContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
`;

export const CardSuccessIcon = styled.div`
  color: ${({ theme }) => theme.colors['yellow-500']};

  svg {
    width: 56px;
    height: 56px;
  }
`;

export const CardSuccessTitle = styled.span`
  font-size: ${({ theme }) => theme.typography.fontSize.title.small};
  font-weight: ${({ theme }) => theme.typography.fontWeight.large};
  color: ${({ theme }) => theme.colors.white};
`;

export const CardSuccessSubtitle = styled.span`
  font-size: ${({ theme }) => theme.typography.fontSize.subTitle.medium};
  color: ${({ theme }) => theme.colors['gray-200']};
  white-space: pre-line;
`;
