export const changeToCloudfrontUrlFromS3 = (filePath: string): string => {
  if (filePath.includes('moment-prod')) {
    const extractImagePathFromS3Url = (url: string) => {
      if (url.includes('s3.ap-northeast-2.amazonaws.com')) {
        const parts = url.split('.amazonaws.com');
        if (parts.length > 1) {
          const fullPath = parts[1];
          return fullPath.replace('/moment-prod', '');
        }
      }
      return url;
    };

    const imagePath = extractImagePathFromS3Url(filePath);
    return `https://d1irdds83bn7at.cloudfront.net${imagePath}`;
  }

  // moment-prod가 아닌 경우 원본 S3 URL 그대로 반환
  return filePath;
};
