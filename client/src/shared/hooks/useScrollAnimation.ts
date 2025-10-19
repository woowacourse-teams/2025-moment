import { useEffect, useRef } from 'react';

interface UseScrollAnimationProps {
  onVisible: () => void;
  threshold?: number;
  rootMargin?: string;
}

export const useScrollAnimation = ({
  onVisible,
  threshold = 0.3,
  rootMargin = '0px 0px -100px 0px',
}: UseScrollAnimationProps) => {
  const elementRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const element = elementRef.current;
    if (!element) return;

    const observer = new window.IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onVisible();
          observer.unobserve(element);
        }
      },
      { threshold, rootMargin },
    );

    observer.observe(element);

    return () => observer.disconnect();
  }, [onVisible, threshold, rootMargin]);

  return elementRef;
};
