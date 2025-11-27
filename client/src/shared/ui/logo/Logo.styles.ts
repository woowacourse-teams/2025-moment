import styled from '@emotion/styled';
import { Picture } from '@/shared/ui/picture';

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

export const LogoImage = styled(Picture)`
  width: clamp(30px, 3vw, 50px);
  height: clamp(30px, 3vw, 50px);

  & img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    object-position: center;
    border-radius: 100%;
  }
`;

export const LogoText = styled.span`
  font-size: 26px;
  color: white;
  font-weight: bold;
`;
