import styled from '@emotion/styled';

export const IconBarContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 60px;

  @media (max-width: 1024px) {
    gap: 30px;
  }
`;

export const LinkContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: row;
  gap: 12px;

  &:hover {
    transform: scale(1.1);
    transition: transform 0.3s ease-in-out;
  }
`;

export const IconImage = styled.img`
  width: 40px;
  height: 40px;
`;

export const IconText = styled.p`
  font-size: 1.3rem;
  font-weight: 600;
  line-height: 14.52px;
  letter-spacing: -0.01em;

  @media (max-width: 1024px) {
    font-size: 1.1rem;
  }
`;

// export const IconBarAside = styled.aside`
//   position: fixed;
//   left: 60px;
//   top: 50%;
//   transform: translateY(-50%);
//   z-index: 50;
