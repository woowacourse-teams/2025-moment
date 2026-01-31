import React, { useCallback, useEffect } from "react";
import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";
import { useFocusEffect, router } from "expo-router";
import { useWebView } from "@/hooks/useWebview";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ErrorScreen } from "@/components/ErrorScreen";
import { COLORS } from "@/constants/theme";
import { usePushNotifications } from "@/hooks/usePushNotifications";
import { useGroup } from "@/context/GroupContext";
import { BridgeMessage } from "@/types/bridge";

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
    let data: BridgeMessage;

    try {
      data = JSON.parse(event.nativeEvent.data);
    } catch (e) {
      console.warn("Bridge: Failed to parse message", event.nativeEvent.data);
      return;
    }

    // 타입이 없으면 무시
    if (!data.type) {
      console.log("Bridge: Message ignored (no type)");
      return;
    }

    try {
      switch (data.type) {
        case "AUTH_REQUEST": {
          if (data.provider === "apple") {
            try {
              const credential = await AppleAuthentication.signInAsync({
                requestedScopes: [
                  AppleAuthentication.AppleAuthenticationScope.FULL_NAME,
                  AppleAuthentication.AppleAuthenticationScope.EMAIL,
                ],
              });

              if (credential.identityToken) {
                // Send token back to WebView
                // TODO: 추후 AUTH_RESULT 메시지로 통일 고려
                const script = `
                  if (window.onAppleLoginSuccess) {
                    window.onAppleLoginSuccess('${credential.identityToken}');
                  }
                `;
                webViewRef.current?.injectJavaScript(script);
              }
            } catch (e: any) {
              if (e.code === "ERR_CANCELED") {
                // User canceled
              } else {
                console.error(e);
              }
            }
          } else if (data.provider === "google") {
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
                // user cancelled
              } else if (error.code === statusCodes.IN_PROGRESS) {
                // operation in progress
              } else if (
                error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE
              ) {
                // play services not available
              } else {
                console.error(error);
              }
            }
          } else {
            console.warn("Bridge: AUTH_REQUEST missing valid provider", data);
          }
          break;
        }

        case "GROUP_CHANGED": {
          if (data.groupId) {
            setGroupId(data.groupId);
            console.log("Native: Group Changed to", data.groupId);
            // 홈 탭으로 이동하여 해당 그룹의 피드를 보여줌
            setTimeout(() => {
              router.push({
                pathname: "/(tabs)",
                params: { refresh: Date.now().toString() },
              });
            }, 0);
          } else {
            console.warn("Bridge: GROUP_CHANGED missing groupId", data);
          }
          break;
        }

        case "TAB_FOCUS": {
          const { tab } = data;
          if (tab === "comment") {
            // We use a small timeout to ensure state update propagates if needed
            setTimeout(() => {
              router.push("/(tabs)/comment");
            }, 0);
          } else if (tab === "home") {
            setTimeout(() => {
              router.push("/(tabs)");
            }, 0);
          } else {
            console.warn("Bridge: TAB_FOCUS missing or invalid tab", data);
          }
          break;
        }

        case "APP_READY":
        case "ROUTE":
        case "AUTH_RESULT":
        case "PUSH_TOKEN":
        case "ERROR":
          // 아직 처리 로직이 없거나 Native -> Web 메시지인 경우
          console.log(`Bridge: Received ${data.type} (not handled)`);
          break;

        default:
          console.log("Bridge: Unknown message type", (data as any).type);
          break;
      }
    } catch (e) {
      // 메시지 처리 중 에러가 발생해도 앱이 죽지 않도록
      console.error("Bridge: Error handling message", e);
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
          userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1 MomentApp"
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
