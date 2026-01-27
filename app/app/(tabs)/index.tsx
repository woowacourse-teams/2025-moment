import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { BASE_URL } from "@/constants/config";
import { useGroup } from "@/context/GroupContext";
import { useLocalSearchParams } from "expo-router";

export default function HomeScreen() {
  const { refresh } = useLocalSearchParams();
  const { currentGroupId } = useGroup();

  const url = currentGroupId
    ? `${BASE_URL}/groups/${currentGroupId}/today-moment`
    : BASE_URL;

  return <WebViewScreen url={url} key={refresh as string} />;
}
