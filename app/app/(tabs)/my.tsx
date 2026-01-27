import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { BASE_URL } from "@/constants/config";
import { useLocalSearchParams } from "expo-router";

export default function MyScreen() {
  const { refresh } = useLocalSearchParams();
  return <WebViewScreen url={`${BASE_URL}/my`} key={refresh as string} />;
}
