import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { BASE_URL } from "@/constants/config";
import { useLocalSearchParams } from "expo-router";

export default function HomeScreen() {
  const { refresh } = useLocalSearchParams();

  return <WebViewScreen url={BASE_URL} key={refresh as string} />;
}
