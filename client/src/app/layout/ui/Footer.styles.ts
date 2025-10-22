import styled from '@emotion/styled';

export const FooterWrapper = styled.div`
  width: 100%;
  height: 100px;
  color: ${({ theme }) => theme.colors['white_70']};
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
`;

export const InquiryLink = styled.a`
  color: ${({ theme }) => theme.colors['white_70']};
  text-decoration: underline;
  &:hover {
    color: ${({ theme }) => theme.colors['yellow-300_80']};
  }
`;
