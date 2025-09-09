import { PROFANITY_WORDS } from './ts/profanityWords';

export const checkProfanityWord = (word: string) => {
  return PROFANITY_WORDS.includes(word);
};
