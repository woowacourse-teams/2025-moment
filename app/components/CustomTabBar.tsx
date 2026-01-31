import React from "react";
import { View, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";

export type TabType = "home" | "moment" | "comment" | "collection" | "my";

interface TabConfig {
  key: TabType;
  title: string;
  icon: keyof typeof Ionicons.glyphMap;
  requiresGroup: boolean;
}

const TABS: TabConfig[] = [
  { key: "home", title: "홈", icon: "home-outline", requiresGroup: false },
  { key: "moment", title: "모멘트", icon: "paper-plane-outline", requiresGroup: true },
  { key: "comment", title: "코멘트", icon: "chatbubble-outline", requiresGroup: true },
  { key: "collection", title: "모음집", icon: "star-outline", requiresGroup: true },
  { key: "my", title: "마이", icon: "person-outline", requiresGroup: false },
];

interface CustomTabBarProps {
  currentTab: TabType;
  onTabPress: (tab: TabType) => void;
  hasGroup: boolean;
}

export function CustomTabBar({ currentTab, onTabPress, hasGroup }: CustomTabBarProps) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingBottom: insets.bottom }]}>
      {TABS.map((tab) => {
        const isActive = currentTab === tab.key;
        const isDisabled = tab.requiresGroup && !hasGroup;

        if (isDisabled) return null;

        return (
          <TouchableOpacity
            key={tab.key}
            style={styles.tab}
            onPress={() => onTabPress(tab.key)}
            activeOpacity={0.7}
          >
            <Ionicons
              name={tab.icon}
              size={24}
              color={isActive ? "#ffffff" : "#888888"}
            />
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    backgroundColor: "#0a0a0f",
    borderTopWidth: 1,
    borderTopColor: "rgba(255,255,255,0.1)",
    paddingTop: 8,
  },
  tab: {
    flex: 1,
    alignItems: "center",
    paddingVertical: 8,
  },
});
