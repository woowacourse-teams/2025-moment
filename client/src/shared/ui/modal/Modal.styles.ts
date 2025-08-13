import styled from '@emotion/styled';

export const ModalWrapper = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  position: fixed;
  width: 100dvw;
  height: 100dvh;
  background-color: ${({ theme }) => theme.colors.black_70};
  color: ${({ theme }) => theme.colors.white};
  z-index: 1000;
  left: 0;
  top: 0;
`;

export const ModalFrame = styled.div<{
  $position: 'center' | 'bottom';
  $size: 'small' | 'medium' | 'large';
}>`
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: ${({ theme, $size }) => theme.typography.cardWidth[$size]};
  padding: 20px 30px;
  background-color: ${({ theme }) => theme.colors['slate-800_60']};
  border-radius: 10px;
  border: 1px solid ${({ theme }) => theme.colors['gray-700']};
`;

export const ModalHeader = styled.div<{ $hasTitle: boolean }>`
  display: flex;
  width: 100%;
  justify-content: ${({ $hasTitle }) => ($hasTitle ? 'space-between' : 'right')};
  align-items: center;
`;
