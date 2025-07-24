import { EMOJI_TYPE } from '../const/emojiType';

export const emojiMapping = (emojiType: string) => {
  return EMOJI_TYPE[emojiType as keyof typeof EMOJI_TYPE] || emojiType;
};
