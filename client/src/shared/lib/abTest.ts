const EXPERIMENTS = {
  'submit-btn-position': { salt: 'sbp-v1', variants: ['control', 'treatment'] as const },
} as const;

export type ExperimentKey = keyof typeof EXPERIMENTS;
export type Variant = 'control' | 'treatment';

export function getUserVariant(key: ExperimentKey, userId: number): Variant {
  const str = String(userId) + EXPERIMENTS[key].salt;
  let hash = 0;
  for (const c of str) hash = (hash * 31 + c.charCodeAt(0)) >>> 0;
  return EXPERIMENTS[key].variants[hash % 2];
}
