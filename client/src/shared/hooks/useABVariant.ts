import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { ExperimentKey, getLocalVariant, getUserVariant, Variant } from '@/shared/lib/abTest';

export function useABVariant(key: ExperimentKey): Variant {
  if (process.env.NODE_ENV === 'development') {
    const param = new URLSearchParams(window.location.search).get('ab');
    if (param === 'control' || param === 'treatment') return param;
  }

  const { data: profile } = useProfileQuery({ enabled: true });
  if (!profile) return getLocalVariant(key);
  return getUserVariant(key, profile.id);
}
