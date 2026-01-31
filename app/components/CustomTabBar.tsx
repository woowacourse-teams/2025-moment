import React from "react";
import { View, TouchableOpacity, StyleSheet, Image, ImageSourcePropType, Text } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

export type TabType = "home" | "moment" | "comment" | "collection" | "my";

interface TabConfig {
  key: TabType;
  title: string;
  icon: ImageSourcePropType;
  requiresGroup: boolean;
}

const TABS: TabConfig[] = [
  { key: "moment", title: "모멘트", icon: require("@/assets/images/paperAirplane.webp"), requiresGroup: true },
  { key: "comment", title: "코멘트", icon: require("@/assets/images/bluePlanet.webp"), requiresGroup: true },
  { key: "home", title: "홈", icon: require("@/assets/images/rocket.webp"), requiresGroup: false },
  { key: "collection", title: "모음집", icon: require("@/assets/images/starPlanet.webp"), requiresGroup: true },
  { key: "my", title: "마이페이지", icon: require("@/assets/images/spaceMan.webp"), requiresGroup: false },
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
            <Image
              source={tab.icon}
              style={[
                styles.icon,
                { opacity: isActive ? 1 : 0.5 },
              ]}
            />
            <Text style={[styles.label, { opacity: isActive ? 1 : 0.5 }]}>
              {tab.title}
            </Text>
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
  icon: {
    width: 24,
    height: 24,
  },
  label: {
    color: "#ffffff",
    fontSize: 10,
    marginTop: 4,
  },
});
