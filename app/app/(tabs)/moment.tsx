import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { BASE_URL } from "@/constants/config";
import { useGroup } from "@/context/GroupContext";
import { useLocalSearchParams } from "expo-router";

export default function MomentScreen() {
  const { refresh } = useLocalSearchParams();
  const { currentGroupId } = useGroup();

  // 그룹이 없으면 홈으로 리다이렉트되므로, 여기선 모멘트만 표시
  const url = currentGroupId
    ? `${BASE_URL}/groups/${currentGroupId}/today-moment`
    : BASE_URL;

  return <WebViewScreen url={url} key={refresh as string} />;
}
