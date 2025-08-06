import styled from '@emotion/styled';

export const IconBarContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 30px;
`;

export const IconWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.2s ease-in-out;
  }
`;

export const IconImage = styled.img`
  width: 70px;
  height: 70px;
`;

export const IconText = styled.p`
  font-size: 20px;
  font-weight: 400;
  line-height: 14.52px;
  letter-spacing: -0.01em;
`;
export const IconBarAside = styled.aside`
  position: fixed;
  left: 60px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 50;

  @media (max-width: 768px) {
    right: 12px;
  }

  @media (max-width: 480px) {
    display: none;
  }
`;
