import { useCallback, useEffect, useRef } from "react";
import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";
import { useFocusEffect, router, usePathname } from "expo-router";

import { useWebView } from "@/hooks/useWebview";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ErrorScreen } from "@/components/ErrorScreen";
import { COLORS } from "@/constants/theme";
import { usePushNotifications } from "@/hooks/usePushNotifications";
import { useGroup } from "@/context/GroupContext";
import { useBridgeMessageHandler } from "@/bridge/useBridgeMessageHandler";
import { getTabFromUrl } from "@/utils/tabRouting";

interface WebViewScreenProps {
  url: string;
}

export function WebViewScreen({ url }: WebViewScreenProps) {
  const { webViewRef, isLoading, error, reload, handlers } = useWebView(url);
  const currentPathname = usePathname();
  const lastNavigatedTabRef = useRef<string | null>(null);

  const { expoPushToken } = usePushNotifications();
  const { setGroupId } = useGroup();
  const { handleMessage } = useBridgeMessageHandler({ webViewRef, setGroupId });

  useFocusEffect(
    useCallback(() => {
      lastNavigatedTabRef.current = null;

      if (webViewRef.current) {
        webViewRef.current.injectJavaScript(`
          if (window.onTabFocus) {
            window.onTabFocus();
          }
        `);
      }
    }, []),
  );

  useEffect(() => {
    if (expoPushToken && webViewRef.current) {
      webViewRef.current.injectJavaScript(`
        if (window.onExpoPushToken) {
          window.onExpoPushToken('${expoPushToken}');
        }
      `);
    }
  }, [expoPushToken]);

  const handleNavigationStateChange = useCallback(
    (navState: any) => {
      handlers.onNavigationStateChange(navState);

      const targetTab = getTabFromUrl(navState.url);
      const currentTab =
        currentPathname === "/" ? "/(tabs)" : `/(tabs)${currentPathname}`;

      if (targetTab && targetTab !== currentTab) {
        if (lastNavigatedTabRef.current !== targetTab) {
          lastNavigatedTabRef.current = targetTab;
          setTimeout(() => {
            router.replace(targetTab);
          }, 0);
        }
      }
    },
    [handlers, currentPathname],
  );

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
          onNavigationStateChange={handleNavigationStateChange}
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
