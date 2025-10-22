import { ModalVariant, ExternalModalVariant } from '@/shared/ui/modal/Modal.styles';

export interface ModalProps {
  children: React.ReactNode;
  position?: 'center' | 'bottom';
  size?: 'small' | 'medium' | 'large';
  height?: string;
  isOpen: boolean;
  onClose: () => void;
  variant?: ModalVariant;
  externalVariant?: ExternalModalVariant;
}

export interface ModalHeader {
  title?: string;
  showCloseButton?: boolean;
  id?: string;
}

export interface ModalContent {
  children: React.ReactNode;
}

export interface ModalFooter {
  children: React.ReactNode;
}

export interface ModalContextType {
  handleClose: () => void;
}
