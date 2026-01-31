import * as AppleAuthentication from "expo-apple-authentication";
import {
  GoogleSignin,
  statusCodes,
} from "@react-native-google-signin/google-signin";
import { WebView } from "react-native-webview";

type AuthRequestMessage = { type: "AUTH_REQUEST"; provider: "apple" | "google" };

export async function handleAuthRequest(
  data: AuthRequestMessage,
  webViewRef: React.RefObject<WebView | null>,
) {
  if (data.provider === "apple") {
    await handleAppleAuth(webViewRef);
  } else if (data.provider === "google") {
    await handleGoogleAuth(webViewRef);
  }
}

async function handleAppleAuth(
  webViewRef: React.RefObject<WebView | null>,
) {
  try {
    const credential = await AppleAuthentication.signInAsync({
      requestedScopes: [
        AppleAuthentication.AppleAuthenticationScope.FULL_NAME,
        AppleAuthentication.AppleAuthenticationScope.EMAIL,
      ],
    });

    if (credential.identityToken) {
      const script = `
        if (window.onAppleLoginSuccess) {
          window.onAppleLoginSuccess('${credential.identityToken}');
        }
      `;
      webViewRef.current?.injectJavaScript(script);
    }
  } catch (e: any) {
    if (e.code === "ERR_CANCELED") {
      // User canceled
    } else {
      console.error(e);
    }
  }
}

async function handleGoogleAuth(
  webViewRef: React.RefObject<WebView | null>,
) {
  try {
    await GoogleSignin.hasPlayServices();
    const userInfo = await GoogleSignin.signIn();

    if (userInfo.data?.idToken) {
      const script = `
        if (window.onGoogleLoginSuccess) {
          window.onGoogleLoginSuccess('${userInfo.data.idToken}');
        }
      `;
      webViewRef.current?.injectJavaScript(script);
    }
  } catch (error: any) {
    if (error.code === statusCodes.SIGN_IN_CANCELLED) {
      // user cancelled
    } else if (error.code === statusCodes.IN_PROGRESS) {
      // operation in progress
    } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
      // play services not available
    } else {
      console.error(error);
    }
  }
}
