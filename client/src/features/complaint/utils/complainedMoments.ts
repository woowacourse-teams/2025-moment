const COMPLAINED_MOMENTS_KEY = 'complainedMoments';

export const getComplainedMoments = (): Set<number> => {
  try {
    const stored = localStorage.getItem(COMPLAINED_MOMENTS_KEY);
    return stored ? new Set(JSON.parse(stored)) : new Set();
  } catch {
    return new Set();
  }
};

export const addComplainedMoment = (momentId: number): void => {
  const complainedMoments = getComplainedMoments();
  complainedMoments.add(momentId);
  localStorage.setItem(COMPLAINED_MOMENTS_KEY, JSON.stringify([...complainedMoments]));
};

export const isComplainedMoment = (momentId: number): boolean => {
  return getComplainedMoments().has(momentId);
};

export const removeComplainedMoment = (momentId: number): void => {
  const complainedMoments = getComplainedMoments();
  complainedMoments.delete(momentId);
  localStorage.setItem(COMPLAINED_MOMENTS_KEY, JSON.stringify([...complainedMoments]));
};
