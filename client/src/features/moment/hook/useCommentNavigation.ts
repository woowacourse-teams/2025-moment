import { useState } from 'react';

export const useCommentNavigation = (totalComments: number) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  const goToPrevious = () => {
    setCurrentIndex(prev => Math.max(0, prev - 1));
  };

  const goToNext = () => {
    setCurrentIndex(prev => Math.min(totalComments - 1, prev + 1));
  };

  const reset = () => {
    setCurrentIndex(0);
  };

  return {
    currentIndex,
    goToPrevious,
    goToNext,
    reset,
    hasPrevious: currentIndex > 0,
    hasNext: currentIndex < totalComments - 1,
  };
};
