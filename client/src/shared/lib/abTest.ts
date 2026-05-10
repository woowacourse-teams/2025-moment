const EXPERIMENTS = {
  'submit-btn-position': { salt: 'sbp-v1', variants: ['control', 'treatment'] as const },
  'landing-cta': { salt: 'lc-v1', variants: ['control', 'treatment'] as const },
} as const;

export type ExperimentKey = keyof typeof EXPERIMENTS;
export type Variant = 'control' | 'treatment';

export function getUserVariant(key: ExperimentKey, userId: number): Variant {
  const str = String(userId) + EXPERIMENTS[key].salt;
  let hash = 0;
  for (const c of str) hash = (hash * 31 + c.charCodeAt(0)) >>> 0;
  return EXPERIMENTS[key].variants[hash % 2];
}

// 비로그인 사용자용 — localStorage로 버킷 고정
export function getLocalVariant(key: ExperimentKey): Variant {
  const storageKey = `ab_${key}`;
  const stored = localStorage.getItem(storageKey);
  if (stored === 'control' || stored === 'treatment') return stored;
  const bucket: Variant = Math.random() < 0.5 ? 'control' : 'treatment';
  localStorage.setItem(storageKey, bucket);
  return bucket;
}
