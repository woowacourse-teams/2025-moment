/**
 * iOS Chrome 또는 iOS 기타 브라우저(Safari 제외)를 감지하는 유틸리티
 * iOS에서는 Safari를 통해 홈 화면에 추가한 PWA만 푸시 알림을 지원
 */

export const isIOSChrome = (): boolean => {
  if (typeof window === 'undefined') return false;

  const userAgent = window.navigator.userAgent.toLowerCase();
  const isIOS = /iphone|ipad|ipod/.test(userAgent);
  const isChrome = /crios/.test(userAgent); // Chrome on iOS

  return isIOS && isChrome;
};

export const isIOSNonSafari = (): boolean => {
  if (typeof window === 'undefined') return false;

  const userAgent = window.navigator.userAgent.toLowerCase();
  const isIOS = /iphone|ipad|ipod/.test(userAgent);
  const isSafari = /safari/.test(userAgent) && !/crios|fxios|edgios/.test(userAgent);
  const isStandalone = (window.navigator as Navigator & { standalone?: boolean }).standalone;

  return isIOS && (!isSafari || isStandalone === false);
};

export const isIOSSafariPWA = (): boolean => {
  if (typeof window === 'undefined') return false;

  const userAgent = window.navigator.userAgent.toLowerCase();
  const isIOS = /iphone|ipad|ipod/.test(userAgent);
  const isStandalone = (window.navigator as Navigator & { standalone?: boolean }).standalone;

  return isIOS && isStandalone === true;
};

export const shouldShowIOSBrowserWarning = (): boolean => {
  return isIOSChrome() || (isIOSNonSafari() && !isIOSSafariPWA());
};
