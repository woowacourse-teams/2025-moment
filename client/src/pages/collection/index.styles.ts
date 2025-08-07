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
  }
`;

export const CollectionHeaderLinkContainer = styled(Link)`
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
