/**
 * 이미지 URL의 확장자를 .webp로 변환
 * @param imageUrl - 원본 이미지 URL
 * @returns .webp 확장자로 변환된 이미지 URL
 */
export const convertToWebp = (imageUrl: string): string => {
  const lastDotIndex = imageUrl.lastIndexOf('.');
  if (lastDotIndex === -1) {
    return `${imageUrl}.webp`;
  }

  return `${imageUrl.substring(0, lastDotIndex)}.webp`;
};
