import { TabType } from "@/components/CustomTabBar";
import { BASE_URL } from "@/constants/config";

export function getTabFromUrl(url: string): TabType | null {
  try {
    const urlObj = new URL(url);
    const pathname = urlObj.pathname;

    // /my 페이지
    if (pathname === "/my" || pathname.startsWith("/my/")) {
      return "my";
    }

    // /groups/{groupId}/today-moment -> 모멘트 탭
    if (pathname.match(/^\/groups\/[^/]+\/today-moment/)) {
      return "moment";
    }

    // /groups/{groupId}/today-comment -> 코멘트 탭
    if (pathname.match(/^\/groups\/[^/]+\/today-comment/)) {
      return "comment";
    }

    // /groups/{groupId}/collection -> 모음집 탭
    if (pathname.match(/^\/groups\/[^/]+\/collection/)) {
      return "collection";
    }

    // 루트 경로 또는 그룹 선택 페이지 -> 홈 탭
    if (pathname === "/" || pathname === "") {
      return "home";
    }

    return null;
  } catch {
    return null;
  }
}

export function getUrlForTab(tab: TabType, groupId: string | null): string {
  switch (tab) {
    case "home":
      return BASE_URL;
    case "moment":
      return groupId ? `${BASE_URL}/groups/${groupId}/today-moment` : BASE_URL;
    case "comment":
      return groupId ? `${BASE_URL}/groups/${groupId}/today-comment` : BASE_URL;
    case "collection":
      return groupId ? `${BASE_URL}/groups/${groupId}/collection/my-moment` : BASE_URL;
    case "my":
      return `${BASE_URL}/my`;
    default:
      return BASE_URL;
  }
}
