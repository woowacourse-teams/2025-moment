import { createPortal } from 'react-dom';
import * as S from './Modal.styles';
import { createContext, useContext, useEffect } from 'react';
import {
  ModalProps,
  ModalContent,
  ModalContextType,
  ModalFooter,
  ModalHeader,
} from '@/shared/types/modal';
import useModalFocus from '@/shared/hooks/useModalFocus';

const ModalContext = createContext<ModalContextType | undefined>(undefined);

export function Modal({
  children,
  position = 'center',
  size = 'medium',
  height,
  isOpen,
  onClose: handleClose,
  variant = 'default',
  externalVariant,
}: ModalProps) {
  if (!isOpen) return null;

  const modalRef = useModalFocus(isOpen);

  const handleCloseByBackdrop = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget) {
      handleClose();
    }
  };

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') handleClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [handleClose]);

  return createPortal(
    <ModalContext.Provider value={{ handleClose }}>
      <S.ModalWrapper onClick={handleCloseByBackdrop}>
        <S.ModalFrame
          variant={variant}
          externalVariant={externalVariant}
          role="dialog"
          aria-modal="true"
          $position={position}
          $size={size}
          $height={height}
          onClick={e => e.stopPropagation()}
          ref={modalRef}
        >
          {children}
        </S.ModalFrame>
      </S.ModalWrapper>
    </ModalContext.Provider>,
    document.body,
  );
}

const Header = ({ title, showCloseButton = true }: ModalHeader) => {
  const context = useContext(ModalContext);
  if (!context) throw new Error('Modal.Header는 Modal 컴포넌트 내부에서 사용되어야 합니다.');

  const { handleClose } = context;

  return (
    <S.ModalHeader $hasTitle={!!title}>
      {title}
      {showCloseButton && <S.ModalCloseButton onClick={handleClose}>X</S.ModalCloseButton>}
    </S.ModalHeader>
  );
};

const Content = ({ children }: ModalContent) => {
  return <S.ModalContent>{children}</S.ModalContent>;
};

const Footer = ({ children }: ModalFooter) => {
  return <S.ModalFooter>{children}</S.ModalFooter>;
};

Modal.Header = Header;
Modal.Content = Content;
Modal.Footer = Footer;
