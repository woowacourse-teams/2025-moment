import { useEffect, useRef } from 'react';
import { track } from '@/shared/lib/ga/track';

export function useScrollDepth() {
  const maxDepth = useRef(0);

  useEffect(() => {
    const onScroll = () => {
      const scrollTop = window.scrollY || document.documentElement.scrollTop;
      const docHeight = document.documentElement.scrollHeight - window.innerHeight;
      const percent =
        docHeight > 0 ? Math.min(100, Math.round((scrollTop / docHeight) * 100)) : 100;
      if (percent > maxDepth.current) maxDepth.current = percent;
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    return () => {
      window.removeEventListener('scroll', onScroll);
      const p = maxDepth.current;
      const bucket: '0' | '25' | '50' | '75' | '100' =
        p >= 100 ? '100' : p >= 75 ? '75' : p >= 50 ? '50' : p >= 25 ? '25' : '0';
      track('scroll_depth', {
        percent_bucket: bucket,
        screen_height: window.innerHeight,
        doc_height: document.documentElement.scrollHeight,
      });
    };
  }, []);
}
