import { useEffect, useRef } from 'react';
import { track } from '@/shared/lib/ga/track';

export function useDwell(surface: 'composer' | 'feed' | 'collection') {
  const start = useRef<number>(0);

  useEffect(() => {
    start.current = Date.now();
    track('dwell_start', { surface });
    return () => {
      const dwell = Math.max(0, Math.round((Date.now() - start.current) / 1000));
      track('dwell_end', { surface, dwell_seconds: dwell });
    };
  }, [surface]);
}
