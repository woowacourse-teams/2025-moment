import { PROFANITY_WORDS } from './ts/profanityWords';

export const checkProfanityWord = (content: string) => {
  if (!content || !content.trim()) return false;

  const normalizedContent = content.trim();

  return PROFANITY_WORDS.some(word => normalizedContent.includes(word));
};
