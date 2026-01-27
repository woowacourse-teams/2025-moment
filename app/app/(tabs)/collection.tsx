import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { useGroup } from "@/context/GroupContext";
import { BASE_URL } from "@/constants/config";

export default function CollectionScreen() {
  const { currentGroupId } = useGroup();

  if (!currentGroupId) return null;

  const url = `${BASE_URL}/groups/${currentGroupId}/collection/my-moment`;

  return <WebViewScreen url={url} />;
}
