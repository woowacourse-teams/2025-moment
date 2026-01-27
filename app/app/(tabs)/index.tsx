import React from "react";
import { WebViewScreen } from "@/components/WebViewScreen";
import { BASE_URL } from "@/constants/config";

export default function HomeScreen() {
  return <WebViewScreen url={BASE_URL} />;
}
