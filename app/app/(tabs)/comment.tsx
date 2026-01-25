import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";

// TODO: Need context to inject groupId dynamically.
// For now, redirecting to root which handles group selection/redirection.
const WEB_URL = "https://connectingmoment.com";

export default function CommentScreen() {
  return <WebViewScreen url={WEB_URL} />;
}
