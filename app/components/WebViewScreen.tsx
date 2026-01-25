import React from "react";
import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";
import { useWebView } from "@/hooks/useWebview";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ErrorScreen } from "@/components/ErrorScreen";
import { COLORS } from "@/constants/theme";

import * as AppleAuthentication from "expo-apple-authentication";

interface WebViewScreenProps {
  url: string;
}

export function WebViewScreen({ url }: WebViewScreenProps) {
  const { webViewRef, isLoading, error, reload, handlers } = useWebView(url);

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
          onNavigationStateChange={handlers.onNavigationStateChange}
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
