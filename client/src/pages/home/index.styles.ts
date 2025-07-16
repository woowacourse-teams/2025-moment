import styled from '@emotion/styled';

export const HomePageWrapper = styled.main`
  width: 100%;
  overflow-x: hidden;
`;

export const HeroSection = styled.section`
  position: relative;
  width: 100%;

  margin-top: 80px;

  @media (max-width: 768px) {
    margin-top: 110px;
  }
`;

export const ContentSection = styled.section`
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 50px;
  padding: 20px;
  overflow: hidden;

  @media (max-width: 768px) {
    margin-top: 60px;
    padding: 16px;
  }

  @media (max-width: 480px) {
    margin-top: 80px;
    padding: 12px;
  }
`;
