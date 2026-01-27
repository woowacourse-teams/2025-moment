import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { useGroup } from "@/context/GroupContext";
import { BASE_URL } from "@/constants/config";
import { useLocalSearchParams } from "expo-router";

export default function CommentScreen() {
  const { currentGroupId } = useGroup();
  const { refresh } = useLocalSearchParams();

  if (!currentGroupId) return null;

  const url = `${BASE_URL}/groups/${currentGroupId}/today-comment`;

  return <WebViewScreen url={url} key={refresh as string} />;
}
