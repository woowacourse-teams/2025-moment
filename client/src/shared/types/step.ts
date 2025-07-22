export const STEPS = ['step1', 'step2', 'step3'] as const;
export type Step = (typeof STEPS)[number];
