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
        "168098995955-rhe4o1m3gnlnab3cpcd4qqv7i3ums1vh.apps.googleusercontent.com",
    });
  }, []);
  return (
    <SafeAreaProvider>
      <GroupProvider>
        <Stack screenOptions={{ headerShown: false }}>
          <Stack.Screen name="(tabs)" />
        </Stack>
        <StatusBar style="light" />
      </GroupProvider>
    </SafeAreaProvider>
  );
}
