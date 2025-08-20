import { CustomTheme } from '@/app/styles/theme';
import styled from '@emotion/styled';

export type ModalVariant = 'default' | 'memoji';
export type ModalSize = 'small' | 'medium' | 'large';
export type ModalPosition = 'center' | 'bottom';
export type ExternalModalVariant = (theme: CustomTheme) => string;

const modalFrameStyles = {
  default: (
    theme: CustomTheme,
    props: { $size: ModalSize; $position: ModalPosition; $height?: string },
  ) => `
    background-color: ${theme.colors['slate-800']};
    border-radius: 10px;
    border: 1px solid ${theme.colors['gray-700']};
    padding: 20px 30px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    width: ${props.$position === 'center' ? theme.typography.modalWidth[props.$size].desktop : '100%'};
    height: ${props.$height || 'auto'};

    ${theme.mediaQueries.tablet} {
      padding: 16px 24px;
      width: ${props.$position === 'center' ? theme.typography.modalWidth[props.$size].tablet : '100%'};
    }
    
    ${theme.mediaQueries.mobile} {
      padding: 12px 20px;
      width: ${props.$position === 'center' ? theme.typography.modalWidth[props.$size].mobile : '100%'};
    }
  `,

  memoji: (theme: CustomTheme) => `
    background-color: ${theme.colors['slate-800']};
    border-radius: 10px;
    border: 1px solid ${theme.colors['gray-700']};
    padding: 20px 30px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    
    width: 500px;
    height: 500px;
    
    ${theme.mediaQueries.tablet} {
      padding: 20px 28px;
      width: 450px
      height: 450px;
    }
    
    ${theme.mediaQueries.mobile} {
      padding: 12px 18px;
      width: 80%;
      height: 400px;
    }
  `,
};

export const ModalFrame = styled.div<{
  $position: 'center' | 'bottom';
  $size: 'small' | 'medium' | 'large';
  $height?: string;
  variant: ModalVariant;
  externalVariant?: ExternalModalVariant;
}>`
  display: flex;
  flex-direction: column;
  gap: 15px;
  position: ${({ $position }) => ($position === 'center' ? 'relative' : 'fixed')};
  bottom: ${({ $position }) => ($position === 'bottom' ? '0' : 'auto')};
  left: ${({ $position }) => ($position === 'bottom' ? '50%' : 'auto')};
  transform: ${({ $position }) => ($position === 'bottom' ? 'translateX(-50%)' : 'none')};

  ${({ theme, variant, $size, $position, $height }) =>
    modalFrameStyles[variant](theme, { $size, $position, $height })};
  ${({ theme, externalVariant }) => externalVariant && externalVariant(theme)};
`;

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

export const ModalCloseButton = styled.button`
  width: 30px;
  height: 30px;
  border-radius: 50%;

  &:hover {
    background-color: ${({ theme }) => theme.colors['gray-700']};
  }
`;

export const ModalHeader = styled.div<{ $hasTitle: boolean }>`
  display: flex;
  width: 100%;
  justify-content: ${({ $hasTitle }) => ($hasTitle ? 'space-between' : 'right')};
  align-items: center;
`;

export const ModalContent = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 10px;
  overflow-y: auto;
`;

export const ModalFooter = styled.div`
  display: flex;
  /* width: 100%; */
  justify-content: flex-end;
  gap: 10px;
`;
