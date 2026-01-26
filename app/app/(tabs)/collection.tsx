import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { useGroup } from "@/context/GroupContext";

const BASE_URL = "https://connectingmoment.com/groups";

export default function CollectionScreen() {
  const { currentGroupId } = useGroup();

  if (!currentGroupId) return null;

  const url = `${BASE_URL}/${currentGroupId}/collection/my-moment`;

  return <WebViewScreen url={url} />;
}
