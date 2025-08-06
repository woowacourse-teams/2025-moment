import { useCallback, useEffect, useRef } from 'react';

interface UseIntersectionObserverProps {
  onIntersect: () => void;
  threshold?: number;
  enabled?: boolean;
}

export const useIntersectionObserver = ({
  onIntersect,
  threshold = 0.1,
  enabled = true,
}: UseIntersectionObserverProps) => {
  const observerRef = useRef<HTMLDivElement>(null);

  const handleIntersect = useCallback(
    (entries: globalThis.IntersectionObserverEntry[]) => {
      const [entry] = entries;
      if (entry.isIntersecting && enabled) {
        onIntersect();
      }
    },
    [onIntersect, enabled],
  );

  useEffect(() => {
    if (!observerRef.current) return;

    const observer = new globalThis.IntersectionObserver(handleIntersect, {
      threshold,
    });

    observer.observe(observerRef.current);

    return () => observer.disconnect();
  }, [handleIntersect, threshold]);

  return observerRef;
};
