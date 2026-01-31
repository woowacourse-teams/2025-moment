import { WebView, WebViewMessageEvent } from "react-native-webview";
import { BridgeMessage } from "@/types/bridge";
import { handleAuthRequest } from "./handlers/authHandler";

interface UseBridgeMessageHandlerProps {
  webViewRef: React.RefObject<WebView | null>;
  setGroupId: (groupId: string) => void;
}

export function useBridgeMessageHandler({
  webViewRef,
  setGroupId,
}: UseBridgeMessageHandlerProps) {
  const handleMessage = async (event: WebViewMessageEvent) => {
    try {
      const data: BridgeMessage = JSON.parse(event.nativeEvent.data);

      if (!data.type) return;

      switch (data.type) {
        case "AUTH_REQUEST":
          await handleAuthRequest(data, webViewRef);
          break;

        case "GROUP_CHANGED":
          setGroupId(data.groupId);
          break;

        case "TAB_FOCUS":
        case "APP_READY":
        case "ROUTE":
        case "AUTH_RESULT":
        case "PUSH_TOKEN":
        case "ERROR":
          break;
      }
    } catch (e) {
      console.error("Bridge: Error handling message", e);
    }
  };

  return { handleMessage };
}
