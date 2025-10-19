import { useEffect, useRef } from 'react';
import { track } from '@/shared/lib/ga/track';

export function useDwell(params: {
  item_type: 'moment' | 'comment';
  surface: 'composer' | 'feed' | 'detail' | string;
  item_id?: string;
}) {
  const start = useRef<number>(0);

  useEffect(() => {
    start.current = Date.now();
    track('dwell_start', params);
    return () => {
      const dwell = Math.max(0, Math.round((Date.now() - start.current) / 1000));
      track('dwell_end', { ...params, dwell_seconds: dwell });
    };
  }, []);
}
