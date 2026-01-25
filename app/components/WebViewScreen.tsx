import React from "react";
import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";
import { useWebView } from "@/hooks/useWebview";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ErrorScreen } from "@/components/ErrorScreen";
import { COLORS } from "@/constants/theme";

interface WebViewScreenProps {
  url: string;
}

export function WebViewScreen({ url }: WebViewScreenProps) {
  const { webViewRef, isLoading, error, reload, handlers } = useWebView(url);

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
