import { ActivityIndicator, StyleSheet, Text, View } from "react-native";

interface LoadingOverlayProps {
  message?: string;
}

export function LoadingOverlay({
  message = "불러오는 중…",
}: LoadingOverlayProps) {
  return (
    <View style={styles.overlay}>
      <ActivityIndicator size="large" color="#ffffff" />
      <Text style={styles.overlayText}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
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
});
