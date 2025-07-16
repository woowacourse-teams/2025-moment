import React from 'react';
import * as S from './StarField.styles';

interface StarFieldProps {
  starCount?: number;
}

export const StarField: React.FC<StarFieldProps> = ({ starCount = 100 }) => {
  const stars = Array.from({ length: starCount }, (_, i) => {
    const size = Math.random() * 2 + 1;
    const left = Math.random() * 100;
    const top = Math.random() * 100;
    const animationDelay = Math.random() * 5;

    return (
      <S.Star key={`star-${i}`} size={size} left={left} top={top} animationDelay={animationDelay} />
    );
  });

  return <S.StarFieldWrapper>{stars}</S.StarFieldWrapper>;
};
