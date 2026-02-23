export const isDevice = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|Windows Phone/i.test(navigator.userAgent);
};

export const isIOS = () => {
  return /iPad|iPhone|iPod/.test(navigator.userAgent);
};

export const isSafari = () => {
  return /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
};

export const isPWA = () => {
  return window.matchMedia('(display-mode: standalone)').matches;
};

export const isApp = () => {
  return /MomentApp/.test(navigator.userAgent);
};

declare global {
  interface Window {
    ReactNativeWebView?: {
      postMessage: (message: string) => void;
    };
    onAppleLoginSuccess?: (token: string) => void;
    onGoogleLoginSuccess?: (token: string) => void;
  }
}
