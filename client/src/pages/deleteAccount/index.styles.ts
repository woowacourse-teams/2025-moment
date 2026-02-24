import styled from '@emotion/styled';

export const PageWrapper = styled.main`
  width: 100%;
  max-width: 600px;
  min-height: 100vh;
  margin: 0 auto;
  padding: 60px 24px;
  background-color: ${({ theme }) => theme.colors['navy-900']};
  color: ${({ theme }) => theme.colors.white};

  @media (max-width: 768px) {
    padding: 40px 16px;
  }
`;

export const Title = styled.h1`
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 16px;
  color: ${({ theme }) => theme.colors.white};

  @media (max-width: 768px) {
    font-size: 21px;
  }
`;

export const Description = styled.p`
  font-size: 15px;
  line-height: 1.8;
  color: ${({ theme }) => theme.colors['gray-200']};
  margin-bottom: 32px;
  word-break: keep-all;
`;

export const InfoBox = styled.div`
  border: 1px solid ${({ theme }) => theme.colors['gray-600']};
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
`;

export const InfoLabel = styled.p`
  font-size: 13px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-bottom: 6px;
`;

export const ContactEmail = styled.a`
  font-size: 17px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['yellow-500']};
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
`;

export const Notice = styled.p`
  font-size: 13px;
  line-height: 1.7;
  color: ${({ theme }) => theme.colors['gray-400']};
  word-break: keep-all;
`;

export const Divider = styled.hr`
  border: none;
  border-top: 1px solid ${({ theme }) => theme.colors['gray-700']};
  margin: 32px 0;
`;

export const SectionTitle = styled.h2`
  font-size: 16px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors['gray-200']};
  margin-bottom: 12px;
`;

export const List = styled.ul`
  margin: 0;
  padding-left: 20px;
`;

export const ListItem = styled.li`
  font-size: 14px;
  line-height: 1.8;
  color: ${({ theme }) => theme.colors['gray-400']};
`;
