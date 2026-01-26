import styled from '@emotion/styled';

export const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
`;

export const ModalContainer = styled.div`
  background-color: #1f1f1f;
  border-radius: 8px;
  min-width: 400px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
`;

export const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #333;
`;

export const Title = styled.h2`
  font-size: 1.25rem;
  font-weight: 600;
`;

export const CloseButton = styled.button`
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 1.5rem;
  cursor: pointer;
  line-height: 1;

  &:hover {
    color: white;
  }
`;

export const Content = styled.div`
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
`;

export const Footer = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid #333;
`;
