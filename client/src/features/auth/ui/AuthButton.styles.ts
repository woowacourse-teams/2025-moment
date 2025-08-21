import styled from '@emotion/styled';

export const AuthButtonContainer = styled.div`
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: ${({ theme }) => theme.colors.white};
  font-size: 16px;
  font-weight: 600;
  padding: 10px;

  transition: all 0.3s ease;
  border-radius: 10px;

  &:hover {
    background-color: ${({ theme }) => theme.colors['slate-900']};
    scale: 1.05;
  }
`;

export const AuthButtonText = styled.p`
  color: ${({ theme }) => theme.colors.white};
`;
export const DropdownContainer = styled.div<{ $isOpen: boolean }>`
  position: absolute;
  top: 100%;
  right: 5;
  margin-top: 8px;
  background-color: ${({ theme }) => theme.colors.white};
  border-radius: 8px;
  overflow: hidden;
  z-index: 1000;
  min-width: 120px;
  border: 1px solid ${({ theme }) => theme.colors.white};

  opacity: ${({ $isOpen }) => ($isOpen ? 1 : 0)};
  visibility: ${({ $isOpen }) => ($isOpen ? 'visible' : 'hidden')};
  transform: ${({ $isOpen }) => ($isOpen ? 'translateY(0)' : 'translateY(-10px)')};
  transition: all 0.2s ease-in-out;
`;

export const DropdownItem = styled.button`
  width: 100%;
  padding: 12px 16px;
  background: ${({ theme }) => theme.colors['slate-800']};
  border: none;
  text-align: center;
  color: ${({ theme }) => theme.colors.white};
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.colors['gray-200']};
  }

  &:last-child {
    border-bottom: none;
  }
`;
