import { useState } from 'react';

export const useSubmitted = () => {
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = () => {
    setIsSubmitted(true);
  };

  return { isSubmitted, handleSubmit };
};
