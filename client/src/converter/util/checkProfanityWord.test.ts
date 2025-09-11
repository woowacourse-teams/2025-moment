import { checkProfanityWord } from './checkProfanityWord';

describe('checkProfanityWord', () => {
  it('필터링 단어를 포함하고 있는 경우 true를 반환한다', () => {
    const content = '눈깔이 아프네';
    const result = checkProfanityWord(content);
    expect(result).toBe(true);
  });
});
