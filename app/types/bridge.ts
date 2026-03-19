export type BridgeMessage =
  | { type: "APP_READY"; version: string }
  | { type: "ROUTE"; url: string }
  | { type: "AUTH_REQUEST"; provider: "apple" | "google" }
  | { type: "AUTH_RESULT"; ok: boolean; token?: string; reason?: string }
  | { type: "PUSH_TOKEN"; token: string }
  | {
      type: "TAB_FOCUS";
      tab: "home" | "moment" | "collection" | "comment" | "my";
    }
  | { type: "GROUP_CHANGED"; groupId: string }
  | { type: "ERROR"; message: string; detail?: unknown };
