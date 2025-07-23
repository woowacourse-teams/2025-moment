import { useState } from 'react';

export const useSubmitted = () => {
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = () => {
    setIsSubmitted(true);
  };

  const handleBack = () => {
    setIsSubmitted(false);
  };

  return { isSubmitted, handleSubmit, handleBack };
};
