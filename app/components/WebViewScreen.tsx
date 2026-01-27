import React, { useCallback, useEffect } from "react";
import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";
import { useFocusEffect } from "expo-router";
import { useWebView } from "@/hooks/useWebview";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ErrorScreen } from "@/components/ErrorScreen";
import { COLORS } from "@/constants/theme";
import { usePushNotifications } from "@/hooks/usePushNotifications";
import { useGroup } from "@/context/GroupContext";

import * as AppleAuthentication from "expo-apple-authentication";

import {
  GoogleSignin,
  statusCodes,
} from "@react-native-google-signin/google-signin";

interface WebViewScreenProps {
  url: string;
}

export function WebViewScreen({ url }: WebViewScreenProps) {
  const { webViewRef, isLoading, error, reload, handlers } = useWebView(url);

  const { expoPushToken } = usePushNotifications();
  const { setGroupId } = useGroup();

  useEffect(() => {
    GoogleSignin.configure({
      // iosClientId is required if GoogleService-Info.plist is missing.
      // We use a placeholder here to prevent immediate crash, but Google Login will fail until plist is added.
      iosClientId: "PLACEHOLDER_CLIENT_ID_FOR_DEV",
      webClientId:
        "567889139262-rn77174628f804562095819385800000.apps.googleusercontent.com",
    });
  }, []);

  // 탭이 포커스될 때 로그인 상태 동기화를 위해 웹뷰에 알림 전송
  useFocusEffect(
    useCallback(() => {
      if (webViewRef.current) {
        // 웹앱에 현재 탭이 포커스되었음을 알림
        // 웹앱은 이를 받아서 로그인 상태를 다시 조회(queryClient.invalidateQueries)함
        const script = `
          if (window.onTabFocus) {
            window.onTabFocus();
          }
        `;
        webViewRef.current.injectJavaScript(script);
      }
    }, []),
  );

  useEffect(() => {
    if (expoPushToken && webViewRef.current) {
      const script = `
        if (window.onExpoPushToken) {
          window.onExpoPushToken('${expoPushToken}');
        }
      `;
      webViewRef.current.injectJavaScript(script);
    }
  }, [expoPushToken]);

  const handleMessage = async (event: any) => {
    try {
      const data = JSON.parse(event.nativeEvent.data);
      if (data.type === "LOGIN_APPLE") {
        try {
          const credential = await AppleAuthentication.signInAsync({
            requestedScopes: [
              AppleAuthentication.AppleAuthenticationScope.FULL_NAME,
              AppleAuthentication.AppleAuthenticationScope.EMAIL,
            ],
          });

          if (credential.identityToken) {
            // Send token back to WebView
            const script = `
              if (window.onAppleLoginSuccess) {
                window.onAppleLoginSuccess('${credential.identityToken}');
              }
            `;
            webViewRef.current?.injectJavaScript(script);
          }
        } catch (e: any) {
          if (e.code === "ERR_CANCELED") {
            // User canceled, do nothing
          } else {
            console.error(e);
          }
        }
      } else if (data.type === "LOGIN_GOOGLE") {
        try {
          await GoogleSignin.hasPlayServices();
          const userInfo = await GoogleSignin.signIn();
          if (userInfo.data?.idToken) {
            const script = `
              if (window.onGoogleLoginSuccess) {
                window.onGoogleLoginSuccess('${userInfo.data.idToken}');
              }
            `;
            webViewRef.current?.injectJavaScript(script);
          }
        } catch (error: any) {
          if (error.code === statusCodes.SIGN_IN_CANCELLED) {
            // user cancelled the login flow
          } else if (error.code === statusCodes.IN_PROGRESS) {
            // operation (e.g. sign in) is in progress already
          } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
            // play services not available or outdated
          } else {
            // some other error happened
            console.error(error);
          }
        }
      } else if (data.type === "GROUP_CHANGED") {
        if (data.groupId) {
          setGroupId(data.groupId);
          console.log("Native: Group Changed to", data.groupId);
        }
      }
    } catch (e) {
      console.error("Failed to parse message", e);
    }
  };

  return (
    <SafeAreaView
      style={[styles.container, { backgroundColor: COLORS.BACKGROUND }]}
      edges={["top", "bottom"]}
    >
      {!error && (
        <WebView
          ref={webViewRef}
          source={{ uri: url }}
          style={styles.webview}
          javaScriptEnabled
          domStorageEnabled
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1"
          onLoadStart={handlers.onLoadStart}
          onLoadEnd={handlers.onLoadEnd}
          onNavigationStateChange={(navState) => {
            console.log(`[WebView - ${url}] Navigated to: ${navState.url}`);
            handlers.onNavigationStateChange(navState);
          }}
          onError={handlers.onError}
          onHttpError={handlers.onHttpError}
          onMessage={handleMessage}
        />
      )}

      {isLoading && !error && <LoadingOverlay />}

      {error && (
        <ErrorScreen
          title={error.title}
          message={error.message}
          onRetry={reload}
          backgroundColor={COLORS.BACKGROUND}
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  webview: { flex: 1, backgroundColor: "transparent" },
});
