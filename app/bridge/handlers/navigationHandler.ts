import { router } from "expo-router";

type GroupChangedMessage = { type: "GROUP_CHANGED"; groupId: string };
type TabFocusMessage = {
  type: "TAB_FOCUS";
  tab: "home" | "moment" | "collection" | "comment" | "my";
};

export function handleGroupChanged(
  data: GroupChangedMessage,
  setGroupId: (groupId: string) => void,
) {
  setGroupId(data.groupId);
  setTimeout(() => {
    router.push({
      pathname: "/(tabs)/moment",
      params: { refresh: Date.now().toString() },
    });
  }, 0);
}

export function handleTabFocus(data: TabFocusMessage) {
  const { tab } = data;

  if (tab === "home") {
    setTimeout(() => {
      router.push("/(tabs)");
    }, 0);
  } else if (tab === "moment") {
    setTimeout(() => {
      router.push("/(tabs)/moment");
    }, 0);
  } else if (tab === "comment") {
    setTimeout(() => {
      router.push("/(tabs)/comment");
    }, 0);
  } else if (tab === "collection") {
    setTimeout(() => {
      router.push("/(tabs)/collection");
    }, 0);
  } else if (tab === "my") {
    setTimeout(() => {
      router.push("/(tabs)/my");
    }, 0);
  } else {
    console.warn("Bridge: TAB_FOCUS unhandled tab", tab);
  }
}
