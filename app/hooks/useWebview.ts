import { useCallback, useEffect, useRef, useState } from "react";
import { Alert, BackHandler, Platform } from "react-native";
import type { WebView } from "react-native-webview";

type WebError = {
  title: string;
  message: string;
};

export function useWebView(webUrl: string) {
  const webViewRef = useRef<WebView>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [canGoBack, setCanGoBack] = useState(false);
  const [error, setError] = useState<WebError | null>(null);

  const reload = useCallback(() => {
    setError(null);
    setIsLoading(true);
    webViewRef.current?.reload();
  }, []);

  const handleLoadStart = useCallback(() => {
    setIsLoading(true);
    setError(null);
  }, []);

  const handleLoadEnd = useCallback(() => {
    setIsLoading(false);
  }, []);

  const handleNavigationStateChange = useCallback((navState: any) => {
    setCanGoBack(navState.canGoBack);
  }, []);

  const handleError = useCallback(() => {
    setIsLoading(false);
    setError({
      title: "페이지를 불러오지 못했어요",
      message: "네트워크 상태를 확인하고 다시 시도해주세요.",
    });
  }, []);

  const handleHttpError = useCallback((e: any) => {
    setIsLoading(false);
    setError({
      title: `서버 오류 (${e.nativeEvent.statusCode})`,
      message: "잠시 후 다시 시도해주세요.",
    });
  }, []);

  // Android 뒤로가기 처리
  useEffect(() => {
    if (Platform.OS !== "android") return;

    const onBackPress = () => {
      if (canGoBack) {
        webViewRef.current?.goBack();
        return true;
      }

      Alert.alert("앱 종료", "앱을 종료할까요?", [
        { text: "취소", style: "cancel" },
        {
          text: "종료",
          style: "destructive",
          onPress: () => BackHandler.exitApp(),
        },
      ]);
      return true;
    };

    const sub = BackHandler.addEventListener("hardwareBackPress", onBackPress);
    return () => sub.remove();
  }, [canGoBack]);

  return {
    webViewRef,
    isLoading,
    error,
    reload,
    handlers: {
      onLoadStart: handleLoadStart,
      onLoadEnd: handleLoadEnd,
      onNavigationStateChange: handleNavigationStateChange,
      onError: handleError,
      onHttpError: handleHttpError,
    },
  };
}
