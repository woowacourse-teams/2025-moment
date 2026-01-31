import { WebView, WebViewMessageEvent } from "react-native-webview";
import { BridgeMessage } from "@/types/bridge";
import { handleAuthRequest } from "./handlers/authHandler";
import { handleGroupChanged, handleTabFocus } from "./handlers/navigationHandler";

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

      if (!data.type) {
        console.log("Bridge: Message ignored (no type)");
        return;
      }

      switch (data.type) {
        case "AUTH_REQUEST":
          await handleAuthRequest(data, webViewRef);
          break;

        case "GROUP_CHANGED":
          handleGroupChanged(data, setGroupId);
          break;

        case "TAB_FOCUS":
          handleTabFocus(data);
          break;

        case "APP_READY":
        case "ROUTE":
        case "AUTH_RESULT":
        case "PUSH_TOKEN":
        case "ERROR":
          console.log(`Bridge: Received ${data.type} (not handled)`);
          break;

        default:
          console.log("Bridge: Unknown message type", (data as any).type);
          break;
      }
    } catch (e) {
      console.error("Bridge: Error handling message", e);
    }
  };

  return { handleMessage };
}
