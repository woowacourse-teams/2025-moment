import React from "react";
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";
import { useWebView } from "@/hooks/use-webview";

const BG = "#0a0a0f";
const WEB_URL = "https://connectingmoment.com";

export default function HomeScreen() {
  const { webViewRef, isLoading, error, reload, handlers } =
    useWebView(WEB_URL);

  return (
    <SafeAreaView
      style={[styles.container, { backgroundColor: BG }]}
      edges={["top", "bottom"]}
    >
      {/* WebView는 에러 발생 시 숨김 */}
      {!error && (
        <WebView
          ref={webViewRef}
          source={{ uri: WEB_URL }}
          style={styles.webview}
          javaScriptEnabled
          domStorageEnabled
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          onLoadStart={handlers.onLoadStart}
          onLoadEnd={handlers.onLoadEnd}
          onNavigationStateChange={handlers.onNavigationStateChange}
          onError={handlers.onError}
          onHttpError={handlers.onHttpError}
        />
      )}

      {/* ✅ 로딩 오버레이 */}
      {isLoading && !error && (
        <View style={styles.overlay}>
          <ActivityIndicator size="large" color="#ffffff" />
          <Text style={styles.overlayText}>불러오는 중…</Text>
        </View>
      )}

      {/* ✅ 에러 화면 */}
      {error && (
        <View style={styles.errorWrap}>
          <Text style={styles.errorTitle}>{error.title}</Text>
          <Text style={styles.errorMsg}>{error.message}</Text>

          <Pressable onPress={reload} style={styles.retryBtn}>
            <Text style={styles.retryText}>다시 시도</Text>
          </Pressable>
        </View>
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  webview: { flex: 1, backgroundColor: "transparent" },

  overlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
    gap: 10,
    backgroundColor: "rgba(10,10,15,0.35)",
  },
  overlayText: {
    color: "white",
    fontSize: 14,
  },

  errorWrap: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 24,
    backgroundColor: BG,
    gap: 10,
  },
  errorTitle: { color: "white", fontSize: 18, fontWeight: "700" },
  errorMsg: {
    color: "rgba(255,255,255,0.75)",
    fontSize: 14,
    textAlign: "center",
  },
  retryBtn: {
    marginTop: 12,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 10,
    backgroundColor: "rgba(255,255,255,0.12)",
  },
  retryText: { color: "white", fontSize: 14, fontWeight: "600" },
});
