import { useEffect } from "react";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { Stack } from "expo-router";
import { StatusBar } from "expo-status-bar";
import { GoogleSignin } from "@react-native-google-signin/google-signin";
import { GroupProvider } from "@/context/GroupContext";

export default function RootLayout() {
  useEffect(() => {
    GoogleSignin.configure({
      iosClientId: "168098995955-ar8an6g4vp44u4lbsqasb5puahdlapil.apps.googleusercontent.com",
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
