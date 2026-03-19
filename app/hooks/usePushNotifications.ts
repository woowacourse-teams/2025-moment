import { useState, useEffect, useRef, useCallback } from "react";
import * as Device from "expo-device";
import * as Notifications from "expo-notifications";
import Constants from "expo-constants";
import { Platform } from "react-native";

// 포그라운드에서 시스템 알림 표시 안 함 (인앱 UI로 처리)
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: false,
    shouldPlaySound: false,
    shouldSetBadge: false,
    shouldShowBanner: false,
    shouldShowList: false,
  }),
});

export interface PushNotificationData {
  title?: string;
  body?: string;
  data?: Record<string, unknown>;
}

interface UsePushNotificationsOptions {
  onNotificationReceived?: (data: PushNotificationData) => void;
  onNotificationTapped?: (data: PushNotificationData) => void;
}

export const usePushNotifications = (options?: UsePushNotificationsOptions) => {
  const [expoPushToken, setExpoPushToken] = useState<string | undefined>();

  const notificationListener = useRef<Notifications.EventSubscription | null>(
    null,
  );
  const responseListener = useRef<Notifications.EventSubscription | null>(null);
  const optionsRef = useRef(options);

  // 옵션이 변경될 때 ref 업데이트
  useEffect(() => {
    optionsRef.current = options;
  }, [options]);

  const extractNotificationData = useCallback(
    (notification: Notifications.Notification): PushNotificationData => ({
      title: notification.request.content.title ?? undefined,
      body: notification.request.content.body ?? undefined,
      data: notification.request.content.data,
    }),
    [],
  );

  async function registerForPushNotificationsAsync() {
    let token;

    if (Platform.OS === "android") {
      await Notifications.setNotificationChannelAsync("default", {
        name: "default",
        importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: "#FF231F7C",
      });
    }

    if (Device.isDevice) {
      const { status: existingStatus } =
        await Notifications.getPermissionsAsync();
      let finalStatus = existingStatus;
      if (existingStatus !== "granted") {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
      }
      if (finalStatus !== "granted") {
        alert("푸시 알림 권한을 허용하지 않으면 알림을 받을 수 없습니다!");
        return;
      }
      try {
        const projectId = Constants.expoConfig?.extra?.eas?.projectId;
        token = (
          await Notifications.getExpoPushTokenAsync(
            projectId ? { projectId } : undefined,
          )
        ).data;
      } catch (e) {
        console.error("Error getting push token:", e);
      }
    }

    return token;
  }

  useEffect(() => {
    registerForPushNotificationsAsync().then((token) =>
      setExpoPushToken(token),
    );

    // 포그라운드에서 푸시 수신 시
    notificationListener.current =
      Notifications.addNotificationReceivedListener((notification) => {
        const data = extractNotificationData(notification);
        optionsRef.current?.onNotificationReceived?.(data);
      });

    // 알림 탭 시 (백그라운드/종료 상태에서)
    responseListener.current =
      Notifications.addNotificationResponseReceivedListener((response) => {
        const data = extractNotificationData(response.notification);
        optionsRef.current?.onNotificationTapped?.(data);
      });

    return () => {
      notificationListener.current && notificationListener.current.remove();
      responseListener.current && responseListener.current.remove();
    };
  }, [extractNotificationData]);

  return {
    expoPushToken,
  };
};
