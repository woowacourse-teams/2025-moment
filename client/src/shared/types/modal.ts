export interface ModalProps {
  children: React.ReactNode;
  position?: 'center' | 'bottom';
  size?: 'small' | 'medium' | 'large';
  isOpen: boolean;
  onClose: () => void;
}

export interface ModalHeader {
  title?: string;
  showCloseButton?: boolean;
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
