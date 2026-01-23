import styled from '@emotion/styled';

export const PageContainer = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 20px;
`;

export const TitleSection = styled.div`
  flex: 1;
`;

export const Title = styled.h1`
  font-size: 28px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.text.primary};
  margin: 0 0 8px 0;
`;

export const Description = styled.p`
  font-size: 14px;
  color: ${({ theme }) => theme.colors.text.secondary};
  margin: 0;
  line-height: 1.6;
`;

export const MemberCount = styled.div`
  font-size: 14px;
  color: ${({ theme }) => theme.colors.text.tertiary};
  margin-top: 8px;
`;

export const Content = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
`;

export const Section = styled.section`
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

export const SectionTitle = styled.h2`
  font-size: 20px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.text.primary};
  margin: 0;
`;

export const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 60px 20px;
`;
