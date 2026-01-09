import { Pressable, StyleSheet, Text, View } from "react-native";

interface ErrorScreenProps {
  title: string;
  message: string;
  onRetry: () => void;
  backgroundColor?: string;
}

export function ErrorScreen({
  title,
  message,
  onRetry,
  backgroundColor = "#0a0a0f",
}: ErrorScreenProps) {
  return (
    <View style={[styles.errorWrap, { backgroundColor }]}>
      <Text style={styles.errorTitle}>{title}</Text>
      <Text style={styles.errorMsg}>{message}</Text>

      <Pressable onPress={onRetry} style={styles.retryBtn}>
        <Text style={styles.retryText}>다시 시도</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  errorWrap: {
    ...StyleSheet.absoluteFillObject,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 24,
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
