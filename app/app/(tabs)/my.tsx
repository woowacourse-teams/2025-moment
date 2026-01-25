import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";

const WEB_URL = "https://connectingmoment.com/my";

export default function MyScreen() {
  return <WebViewScreen url={WEB_URL} />;
}
