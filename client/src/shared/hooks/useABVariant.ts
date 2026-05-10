import { useProfileQuery } from '@/features/auth/api/useProfileQuery';
import { ExperimentKey, getUserVariant, Variant } from '@/shared/lib/abTest';

export function useABVariant(key: ExperimentKey): Variant {
  const { data: profile } = useProfileQuery({ enabled: true });
  if (!profile) return 'control';
  return getUserVariant(key, profile.id);
}
