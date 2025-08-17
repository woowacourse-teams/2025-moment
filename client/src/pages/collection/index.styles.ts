import styled from '@emotion/styled';
import { Link } from 'react-router';

export const CollectionContainer = styled.section`
  display: flex;
  flex-direction: column;
  gap: 50px;
  margin: 20px;

  @media (max-width: 768px) {
    gap: 30px;
  }
`;

export const CollectionHeaderContainer = styled.div`
  width: 60%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 60px;
  margin: 0 auto;
  border: 1px solid #ccc;
  padding: 20px;

  @media (max-width: 768px) {
    width: 90%;
    gap: 30px;
  }
`;

export const CollectionHeaderLinkContainer = styled(Link)<{ $shadow?: boolean }>`
  ${({ theme, $shadow }) =>
    $shadow &&
    css`
      box-shadow: 0px 0px 15px ${theme.colors['yellow-300_80']};
      animation: shadowPulse 2s ease-in-out infinite;

      @keyframes shadowPulse {
        0%,
        100% {
          box-shadow: 0px 0px 10px ${theme.colors['yellow-300_80']};
        }
        50% {
          box-shadow: 0px 0px 25px ${theme.colors['yellow-300_80']};
        }
      }
    `}

  color: white;
  font-size: 1.5rem;
  font-weight: bold;

  &.active {
    font-size: 1.8rem;
    color: yellow;
  }

  &:hover {
    scale: 1.05;
    transition: all 0.2s ease-in-out;
  }

  @media (max-width: 768px) {
    font-size: 1.2rem;

    &.active {
      font-size: 1.4rem;
    }
  }
`;
