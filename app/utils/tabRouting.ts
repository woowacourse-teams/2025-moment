export type TabRoute =
  | "/(tabs)"
  | "/(tabs)/moment"
  | "/(tabs)/comment"
  | "/(tabs)/collection"
  | "/(tabs)/my";

export function getTabFromUrl(url: string): TabRoute | null {
  try {
    const urlObj = new URL(url);
    const pathname = urlObj.pathname;

    // /my 페이지
    if (pathname === "/my" || pathname.startsWith("/my/")) {
      return "/(tabs)/my";
    }

    // /groups/{groupId}/today-moment -> 모멘트 탭
    if (pathname.match(/^\/groups\/[^/]+\/today-moment/)) {
      return "/(tabs)/moment";
    }

    // /groups/{groupId}/today-comment -> 코멘트 탭
    if (pathname.match(/^\/groups\/[^/]+\/today-comment/)) {
      return "/(tabs)/comment";
    }

    // /groups/{groupId}/collection -> 모음집 탭
    if (pathname.match(/^\/groups\/[^/]+\/collection/)) {
      return "/(tabs)/collection";
    }

    // 루트 경로 또는 그룹 선택 페이지 -> 홈 탭 (index)
    if (pathname === "/" || pathname === "") {
      return "/(tabs)";
    }

    return null;
  } catch {
    return null;
  }
}
