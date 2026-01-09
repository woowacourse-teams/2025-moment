import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { WebView } from "react-native-webview";

const BG = "#0a0a0f";

export default function HomeScreen() {
  // const webUrl = __DEV__
  //   ? "http://192.168.219.104:3000"
  //   : "https://connectingmoment.com";
  const webUrl = "https://connectingmoment.com";

  return (
    <SafeAreaView
      style={[styles.container, { backgroundColor: BG }]}
      edges={["top", "bottom", "left", "right"]}
    >
      <WebView source={{ uri: webUrl }} style={styles.webview} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  webview: { flex: 1, backgroundColor: "transparent" },
});
