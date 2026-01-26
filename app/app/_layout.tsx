import { SafeAreaProvider } from "react-native-safe-area-context";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { GroupProvider } from "@/context/GroupContext";

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <GroupProvider>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        </Stack>
        <StatusBar style="light" />
      </GroupProvider>
    </SafeAreaProvider>
  );
}
