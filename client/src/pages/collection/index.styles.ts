import { css } from '@emotion/react';
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
  padding: 20px;

  @media (max-width: 768px) {
    width: 100%;
    gap: 20px;
  }
`;

export const CollectionHeaderLinkContainer = styled(Link, {
  shouldForwardProp: prop => prop !== '$shadow',
})<{ $shadow?: boolean }>`
  ${({ theme, $shadow }) =>
    $shadow &&
    css`
      position: relative;
      padding: 4px;
      &::after {
        content: '';
        position: absolute;
        top: -6px;
        right: -6px;
        width: 10px;
        height: 10px;
        background: ${theme.colors['yellow-300']};
        border-radius: 50%;
        box-shadow:
          0 0 4px ${theme.colors['yellow-300']},
          0 0 8px ${theme.colors['yellow-300_80']};
        animation: dotPulse 2s ease-in-out infinite;
      }

      @keyframes dotPulse {
        0%,
        100% {
          transform: scale(0.6);
          box-shadow:
            0 0 2px ${theme.colors['yellow-300']},
            0 0 4px ${theme.colors['yellow-300_80']};
        }
        50% {
          transform: scale(1);
          box-shadow:
            0 0 4px ${theme.colors['yellow-300']},
            0 0 8px ${theme.colors['yellow-300_80']};
        }
      }
    `}

  color: ${({ theme }) => theme.colors.white};
  font-size: 2rem;
  font-weight: bold;

  &.active {
    font-size: 2.4rem;
    color: ${({ theme }) => theme.colors['yellow-300']};
    border-bottom: 2.5px solid ${({ theme }) => theme.colors['yellow-300']};
  }

  &:hover {
    scale: 1.05;
    transition: all 0.2s ease-in-out;
  }

  @media (max-width: 1024px) {
    font-size: 1.6rem;

    &.active {
      font-size: 2rem;
    }
  }

  @media (max-width: 768px) {
    font-size: 1.2rem;

    &.active {
      font-size: 1.6rem;
    }
  }
`;

export const Description = styled.p`
  font-size: 1.4rem;
  color: ${({ theme }) => theme.colors.white};
  font-weight: bold;
  margin: 0 auto;

  @media (max-width: 768px) {
    font-size: 1.2rem;
  }
`;

export const FilterWrapper = styled.div`
  display: flex;
  width: 60%;
  justify-content: flex-end;
  margin: 0 auto;
  gap: 4px;

  @media (max-width: 768px) {
    width: 90%;
  }
`;
