import React, { useCallback, useEffect, useMemo, useState } from "react";
import { StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";

import { CustomTabBar, TabType } from "@/components/CustomTabBar";
import { LoadingOverlay } from "@/components/LoadingOverlay";
import { ErrorScreen } from "@/components/ErrorScreen";
import { COLORS } from "@/constants/theme";
import { BASE_URL } from "@/constants/config";
import {
  usePushNotifications,
  PushNotificationData,
} from "@/hooks/usePushNotifications";
import { useGroup } from "@/context/GroupContext";
import { useBridgeMessageHandler } from "@/bridge/useBridgeMessageHandler";
import { getTabFromUrl, getUrlForTab } from "@/utils/tabRouting";
import { useWebView } from "@/hooks/useWebview";

const DISABLE_ZOOM_SCRIPT = `
  (function() {
    var meta = document.querySelector('meta[name="viewport"]');
    if (!meta) {
      meta = document.createElement('meta');
      meta.name = 'viewport';
      document.head.appendChild(meta);
    }
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';

    document.addEventListener('gesturestart', function(e) { e.preventDefault(); }, { passive: false });
    document.addEventListener('gesturechange', function(e) { e.preventDefault(); }, { passive: false });
    document.addEventListener('gestureend', function(e) { e.preventDefault(); }, { passive: false });
  })();
  true;
`;

export default function MainScreen() {
  const [currentTab, setCurrentTab] = useState<TabType>("home");
  const { currentGroupId, setGroupId } = useGroup();

  const { webViewRef, isLoading, error, reload, handlers } = useWebView();

  // 포그라운드에서 푸시 수신 시 WebView에 전달
  const handleNotificationReceived = useCallback(
    (data: PushNotificationData) => {
      webViewRef.current?.injectJavaScript(`
        if (window.onPushNotification) {
          window.onPushNotification(${JSON.stringify(data)});
        }
        true;
      `);
    },
    [webViewRef],
  );

  // 알림 탭 시 해당 화면으로 이동
  const handleNotificationTapped = useCallback(
    (data: PushNotificationData) => {
      const link = data.data?.link as string | undefined;
      if (link) {
        webViewRef.current?.injectJavaScript(`
          window.location.href = '${link}';
          true;
        `);
      }
    },
    [webViewRef],
  );

  const pushOptions = useMemo(
    () => ({
      onNotificationReceived: handleNotificationReceived,
      onNotificationTapped: handleNotificationTapped,
    }),
    [handleNotificationReceived, handleNotificationTapped],
  );

  const { expoPushToken } = usePushNotifications(pushOptions);
  const { handleMessage } = useBridgeMessageHandler({ webViewRef, setGroupId });

  // 푸시 토큰 전달
  useEffect(() => {
    if (expoPushToken && webViewRef.current) {
      webViewRef.current.injectJavaScript(`
        if (window.onExpoPushToken) {
          window.onExpoPushToken('${expoPushToken}');
        }
      `);
    }
  }, [expoPushToken]);

  // 탭 선택 시 URL 변경
  const handleTabPress = useCallback(
    (tab: TabType) => {
      if (tab === currentTab) return;

      const url = getUrlForTab(tab, currentGroupId);
      setCurrentTab(tab);

      webViewRef.current?.injectJavaScript(`
        window.location.href = '${url}';
        true;
      `);
    },
    [currentTab, currentGroupId],
  );

  // WebView URL 변경 감지 → 탭 동기화
  const handleNavigationStateChange = useCallback(
    (navState: any) => {
      handlers.onNavigationStateChange(navState);

      const detectedTab = getTabFromUrl(navState.url);
      if (detectedTab && detectedTab !== currentTab) {
        setCurrentTab(detectedTab);
      }
    },
    [handlers, currentTab],
  );

  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.safeArea} edges={["top"]}>
        {!error && (
          <WebView
            ref={webViewRef}
            source={{ uri: BASE_URL }}
            style={styles.webview}
            javaScriptEnabled
            domStorageEnabled
            sharedCookiesEnabled
            thirdPartyCookiesEnabled
            userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1 MomentApp"
            scalesPageToFit={false}
            setBuiltInZoomControls={false}
            injectedJavaScript={DISABLE_ZOOM_SCRIPT}
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

      <CustomTabBar
        currentTab={currentTab}
        onTabPress={handleTabPress}
        hasGroup={!!currentGroupId}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: COLORS.BACKGROUND,
  },
  safeArea: {
    flex: 1,
  },
  webview: {
    flex: 1,
    backgroundColor: "transparent",
  },
});
