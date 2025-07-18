import { useEffect, useState } from 'react';

interface UseDelayedVisibleProps {
  delay?: number;
  initialVisible?: boolean;
}

export const useDelayedVisible = ({
  delay = 100,
  initialVisible = false,
}: UseDelayedVisibleProps = {}) => {
  const [isVisible, setIsVisible] = useState(initialVisible);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(true);
    }, delay);

    return () => clearTimeout(timer);
  }, [delay]);

  return { isVisible, setIsVisible };
};
