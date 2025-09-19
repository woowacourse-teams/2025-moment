import { useState } from 'react';
import { createPortal } from 'react-dom';

export const useShowFullImage = () => {
  const [fullImageSrc, setFullImageSrc] = useState<string | null>(null);

  const handleImageClick = (imageUrl: string, event: React.MouseEvent) => {
    event.stopPropagation();
    setFullImageSrc(imageUrl);
  };

  const closeFullImage = () => {
    setFullImageSrc(null);
  };

  const ImageOverlayPortal = ({ children }: { children: React.ReactNode }) => {
    return createPortal(children, document.body);
  };

  return {
    fullImageSrc,
    handleImageClick,
    closeFullImage,
    ImageOverlayPortal,
  };
};
