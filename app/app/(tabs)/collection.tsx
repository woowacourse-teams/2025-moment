import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";

// TODO: Update to specific collection URL once groupId context is available
const WEB_URL = "https://connectingmoment.com";

export default function CollectionScreen() {
  return <WebViewScreen url={WEB_URL} />;
}
