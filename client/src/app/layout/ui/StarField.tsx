import React, { useEffect, useRef, useCallback } from 'react';
import styled from '@emotion/styled';

interface StarFieldProps {
  starCount?: number;
}

const CanvasWrapper = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
`;

const StarCanvas = styled.canvas`
  width: 100%;
  height: 100%;
  display: block;
`;

interface Star {
  x: number;
  y: number;
  radius: number;
  opacity: number;
  fadeDirection: number;
  fadeSpeed: number;
}

export const StarField: React.FC<StarFieldProps> = ({ starCount = 80 }) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const animationRef = useRef<number | null>(null);
  const starsRef = useRef<Star[]>([]);

  const initializeStars = useCallback(
    (width: number, height: number) => {
      starsRef.current = Array.from({ length: starCount }, () => ({
        x: Math.random() * width,
        y: Math.random() * height,
        radius: Math.random() * 1.5 + 0.5,
        opacity: Math.random() * 0.8 + 0.2,
        fadeDirection: Math.random() > 0.5 ? 1 : -1,
        fadeSpeed: Math.random() * 0.02 + 0.005,
      }));
    },
    [starCount],
  );

  const animate = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    starsRef.current.forEach(star => {
      star.opacity += star.fadeDirection * star.fadeSpeed;

      if (star.opacity >= 1) {
        star.opacity = 1;
        star.fadeDirection = -1;
      } else if (star.opacity <= 0.2) {
        star.opacity = 0.2;
        star.fadeDirection = 1;
      }

      ctx.beginPath();
      ctx.arc(star.x, star.y, star.radius, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(255, 255, 255, ${star.opacity})`;
      ctx.fill();

      if (star.opacity > 0.7) {
        ctx.beginPath();
        ctx.arc(star.x, star.y, star.radius * 2, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(255, 255, 255, ${(star.opacity - 0.7) * 0.3})`;
        ctx.fill();
      }
    });

    animationRef.current = window.requestAnimationFrame(animate);
  }, []);

  const handleResize = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const { devicePixelRatio: ratio = 1 } = window;
    const rect = canvas.getBoundingClientRect();

    canvas.width = rect.width * ratio;
    canvas.height = rect.height * ratio;

    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.scale(ratio, ratio);
    }

    initializeStars(rect.width, rect.height);
  }, [initializeStars]);

  useEffect(() => {
    handleResize();

    animate();

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      if (animationRef.current) {
        window.cancelAnimationFrame(animationRef.current);
      }
    };
  }, [animate, handleResize]);

  return (
    <CanvasWrapper>
      <StarCanvas ref={canvasRef} />
    </CanvasWrapper>
  );
};
