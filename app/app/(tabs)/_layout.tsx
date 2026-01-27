import { Tabs } from "expo-router";
import React from "react";
import { Ionicons } from "@expo/vector-icons";
import { useGroup } from "@/context/GroupContext";

export default function TabLayout() {
  const { currentGroupId } = useGroup();
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: "#0a0a0f",
          borderTopColor: "rgba(255,255,255,0.1)",
        },
        tabBarActiveTintColor: "#ffffff",
        tabBarInactiveTintColor: "#888888",
      }}
    >
      <Tabs.Screen
        name="index"
        listeners={({ navigation }) => ({
          tabPress: (e) => {
            navigation.setParams({ refresh: Date.now() });
          },
        })}
        options={{
          title: "모멘트",
          tabBarIcon: ({ color }) => (
            <Ionicons name="paper-plane-outline" size={24} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="comment"
        listeners={({ navigation }) => ({
          tabPress: (e) => {
            navigation.setParams({ refresh: Date.now() });
          },
        })}
        options={{
          title: "코멘트",
          href: currentGroupId ? "/comment" : null,
          tabBarIcon: ({ color }) => (
            <Ionicons name="chatbubble-outline" size={24} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="collection"
        listeners={({ navigation }) => ({
          tabPress: (e) => {
            navigation.setParams({ refresh: Date.now() });
          },
        })}
        options={{
          title: "모음집",
          href: currentGroupId ? "/collection" : null,
          tabBarIcon: ({ color }) => (
            <Ionicons name="star-outline" size={24} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="my"
        listeners={({ navigation }) => ({
          tabPress: (e) => {
            navigation.setParams({ refresh: Date.now() });
          },
        })}
        options={{
          title: "마이",
          tabBarIcon: ({ color }) => (
            <Ionicons name="person-outline" size={24} color={color} />
          ),
        }}
      />
    </Tabs>
  );
}
