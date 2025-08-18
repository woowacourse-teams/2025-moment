import { RefObject, useCallback, useEffect } from 'react';

interface UseOutsideClickProps {
  ref: RefObject<HTMLElement | null>;
  callback: () => void;
  isActive?: boolean;
  excludeRefs?: RefObject<HTMLElement | null>[];
}

export const useOutsideClick = ({
  ref,
  callback,
  isActive,
  excludeRefs = [],
}: UseOutsideClickProps) => {
  const handleClickOutside = useCallback(
    (e: MouseEvent) => {
      const target = e.target as Node;

      if (ref.current && ref.current.contains(target)) {
        return;
      }

      for (const excludeRef of excludeRefs) {
        if (excludeRef.current && excludeRef.current.contains(target)) {
          return;
        }
      }

      callback();
    },
    [ref, callback, excludeRefs],
  );

  useEffect(() => {
    if (!isActive) return;

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isActive, handleClickOutside]);
};
