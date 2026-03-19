import ReactGA from 'react-ga4';
import { isGAEnabled } from '.';

type CommonParams = {
  screen?: string;
};

type EventMap = {
  // Group 관련 이벤트
  select_group: { source?: 'home' | 'my_page' };
  create_group: Record<string, never>;
  join_group: Record<string, never>;
  leave_group: Record<string, never>;
  invite_member: Record<string, never>;

  // Moment/Comment 이벤트
  give_likes: { item_type: 'moment' | 'comment' };
  open_composer: { entry?: 'nav' | 'cta' | 'reminder'; composer: 'moment' | 'comment' };
  publish_moment: { has_media?: boolean; content_length_bucket?: 's' | 'm' | 'l' };
  submit_comment: { length_bucket?: 's' | 'm' | 'l' };
  abandon_composer: {
    composer: 'moment' | 'comment';
    has_media?: boolean;
    content_length_bucket?: 's' | 'm' | 'l';
  };

  // Dwell 이벤트
  dwell_start: { surface: 'composer' | 'feed' | 'collection' };
  dwell_end: { surface: 'composer' | 'feed' | 'collection'; dwell_seconds: number };

  // 스크롤/네비게이션 이벤트
  scroll_depth: { percent_bucket: '0' | '25' | '50' | '75' | '100' };
  click_navigation: { destination: 'today_moment' | 'today_comment' | 'collection' };
  click_auth: { device: 'desktop' | 'mobile' };
  click_cta: { cta_type: 'primary' | 'secondary' };
};

function getCommonParams(): CommonParams {
  return {
    screen: window.location.pathname,
    // ab_variant: (window as any).__AB_VARIANT__, // 사용 시 주석 해제
  };
}

export function track<E extends keyof EventMap>(
  eventName: E,
  params: EventMap[E] & CommonParams = {} as any,
) {
  if (!isGAEnabled()) return;
  ReactGA.event(eventName as string, { ...getCommonParams(), ...params });
}
