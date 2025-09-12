/**
 * CloudFront URL 생성을 위한 커스텀 훅
 */
export const useCloudFrontUrl = () => {
  const generateCloudFrontUrl = (filePath: string): string => {
    const currentHostname = window.location.hostname;

    // S3 URL에서 경로 부분만 추출
    const extractPathFromS3Url = (url: string) => {
      if (url.includes('s3.ap-northeast-2.amazonaws.com')) {
        // S3 URL에서 버킷명 이후 경로만 추출
        const parts = url.split('.amazonaws.com');
        if (parts.length > 1) {
          return parts[1]; // /moment-dev/images/... 부분
        }
      }
      return url; // 이미 경로만 있는 경우
    };

    const imagePath = extractPathFromS3Url(filePath);

    if (currentHostname.includes('dev') || currentHostname === 'localhost') {
      // 개발 환경: dev 도메인 사용
      return `https://dev.connectingmoment.com${imagePath}`;
    } else {
      // 프로덕션 환경: moment-dev를 moment로 변경
      const prodPath = imagePath.replace('/moment-dev/', '/moment/');
      return `https://connectingmoment.com${prodPath}`;
    }
  };

  return { generateCloudFrontUrl };
};
