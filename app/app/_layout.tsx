import { useEffect } from "react";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { GoogleSignin } from "@react-native-google-signin/google-signin";
import { GroupProvider } from "@/context/GroupContext";

export default function RootLayout() {
  useEffect(() => {
    GoogleSignin.configure({
      iosClientId: "PLACEHOLDER_CLIENT_ID_FOR_DEV",
      webClientId:
        "567889139262-rn77174628f804562095819385800000.apps.googleusercontent.com",
    });
  }, []);
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
