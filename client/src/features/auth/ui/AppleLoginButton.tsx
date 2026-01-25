import { useEffect, useState } from 'react';
import * as S from './AppleLoginButton.styles';

export const AppleLoginButton = () => {
  const [isWebView, setIsWebView] = useState(false);

  useEffect(() => {
    if (typeof window !== 'undefined' && (window as any).ReactNativeWebView) {
      setIsWebView(true);
    }
  }, []);

  useEffect(() => {
    const handleMessage = (event: any) => {
      try {
        const data = JSON.parse(event.data);
        if (data.type === 'APPLE_LOGIN_SUCCESS') {
          // TODO: Send token to backend
          console.log('Apple Login Success:', data.token);
          alert('Apple 로그인 성공! (백엔드 연동 필요)\nToken: ' + data.token.slice(0, 10) + '...');
        }
      } catch (e) {
        // Ignore non-JSON messages
      }
    };

    if (typeof window !== 'undefined') {
      // For iOS WebView, messages might come differently, but typically document or window.
      // react-native-webview injectJavaScript usually executes code.
      // So we might need to expose a global function.
      (window as any).onAppleLoginSuccess = (token: string) => {
        console.log('Apple Login Success:', token);
        alert('Apple 로그인 성공! (백엔드 연동 필요)\nToken: ' + token.slice(0, 10) + '...');
      };
    }
  }, []);

  const handleAppleLogin = () => {
    if (isWebView && (window as any).ReactNativeWebView) {
      (window as any).ReactNativeWebView.postMessage(JSON.stringify({ type: 'LOGIN_APPLE' }));
    } else {
      alert('Apple 로그인은 앱에서만 가능합니다.');
    }
  };

  if (!isWebView) return null;

  return (
    <S.AppleLoginButton type="button" onClick={handleAppleLogin} aria-label="Apple 기기로 로그인">
      <S.AppleLoginButtonIcon src="/images/apple.png" alt="" />
      Sign in with Apple
    </S.AppleLoginButton>
  );
};
