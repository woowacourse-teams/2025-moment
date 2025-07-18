import styled from '@emotion/styled';

export const LogoButton = styled.button`
  background-color: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    transform: scale(1.05);
    transition: transform 0.3s ease;
  }
`;

export const LogoImage = styled.img`
  width: clamp(30px, 3vw, 50px);
  height: clamp(30px, 3vw, 50px);
  object-fit: contain;
  object-position: center;
  border-radius: 100%;
`;

export const LogoText = styled.span`
  font-size: 26px;
  color: white;
  font-weight: bold;
`;
