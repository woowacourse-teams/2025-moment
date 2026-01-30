import { useEffect, useState } from 'react';
import * as S from './AppleLoginButton.styles';
import { useAppleLoginMutation } from '../api/useAppleLoginMutation';

export const AppleLoginButton = () => {
  const [isWebView, setIsWebView] = useState(false);

  const { mutate: appleLogin } = useAppleLoginMutation();

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
          console.log('Apple Login Success:', data.token);
          appleLogin({ identityToken: data.token });
        }
      } catch (e) {
        // Ignore non-JSON messages
      }
    };

    if (typeof window !== 'undefined') {
      window.addEventListener('message', handleMessage);

      // For iOS WebView, messages might come differently, but typically document or window.
      // react-native-webview injectJavaScript usually executes code.
      // So we might need to expose a global function.
      (window as any).onAppleLoginSuccess = (token: string) => {
        console.log('Apple Login Success:', token);
        appleLogin({ identityToken: token });
      };
    }

    return () => {
      if (typeof window !== 'undefined') {
        window.removeEventListener('message', handleMessage);
      }
    };
  }, [appleLogin]);

  const handleAppleLogin = () => {
    if (isWebView && (window as any).ReactNativeWebView) {
      (window as any).ReactNativeWebView.postMessage(
        JSON.stringify({ type: 'AUTH_REQUEST', provider: 'apple' }),
      );
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
