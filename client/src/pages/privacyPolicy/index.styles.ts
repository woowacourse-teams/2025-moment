import styled from '@emotion/styled';

export const PageWrapper = styled.main`
  width: 100%;
  max-width: 800px;
  min-height: 100vh;
  margin: 0 auto;
  padding: 40px 24px;
  background-color: ${({ theme }) => theme.colors['navy-900']};
  color: ${({ theme }) => theme.colors.white};

  @media (max-width: 768px) {
    padding: 24px 16px;
  }
`;

export const Title = styled.h1`
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: ${({ theme }) => theme.colors.white};

  @media (max-width: 768px) {
    font-size: 22px;
  }
`;

export const LastUpdated = styled.p`
  font-size: 13px;
  color: ${({ theme }) => theme.colors['gray-400']};
  margin-bottom: 32px;
`;

export const Section = styled.section`
  margin-bottom: 28px;
`;

export const SectionTitle = styled.h2`
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: ${({ theme }) => theme.colors['yellow-500']};

  @media (max-width: 768px) {
    font-size: 17px;
  }
`;

export const Paragraph = styled.p`
  font-size: 15px;
  line-height: 1.7;
  color: ${({ theme }) => theme.colors['gray-200']};
  margin: 0 0 8px;
  word-break: keep-all;
`;

export const List = styled.ul`
  margin: 8px 0;
  padding-left: 20px;
`;

export const ListItem = styled.li`
  font-size: 15px;
  line-height: 1.7;
  color: ${({ theme }) => theme.colors['gray-200']};
  margin-bottom: 4px;
`;

export const ContactEmail = styled.a`
  color: ${({ theme }) => theme.colors['yellow-500']};
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
`;
