import { useEffect, useRef } from 'react';

const FOCUSABLE_ELEMENTS =
  'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])';

const useModalFocus = (isOpen: boolean) => {
  const modalRef = useRef<HTMLDivElement>(null);
  const focusableElementsRef = useRef<NodeListOf<Element> | null>(null);

  useEffect(() => {
    if (isOpen && modalRef.current) {
      focusableElementsRef.current = modalRef.current.querySelectorAll(FOCUSABLE_ELEMENTS);
      const focusableElements = focusableElementsRef.current;

      if (focusableElements.length > 0) {
        (focusableElements[0] as HTMLElement).focus();
      } else {
        modalRef.current.setAttribute('tabindex', '-1');
        modalRef.current.focus();
      }

      const handleKeyDown = (event: KeyboardEvent) => {
        if (event.key === 'Tab') {
          if (!focusableElementsRef.current || focusableElementsRef.current.length === 0) return;
          const focusableElements = focusableElementsRef.current;

          const firstElement = focusableElements[0] as HTMLElement;
          const lastElement = focusableElements[focusableElements.length - 1] as HTMLElement;

          if (event.shiftKey && document.activeElement === firstElement) {
            lastElement.focus();
            event.preventDefault();
          } else if (!event.shiftKey && document.activeElement === lastElement) {
            firstElement.focus();
            event.preventDefault();
          }
        }
      };

      document.addEventListener('keydown', handleKeyDown);
      return () => document.removeEventListener('keydown', handleKeyDown);
    }
  }, [isOpen]);

  return modalRef;
};

export default useModalFocus;
