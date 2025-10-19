import ReactGA from 'react-ga4';

type CommonParams = {
  screen?: string;
  entry?: 'nav' | 'cta' | 'reminder' | string;
  item_id?: string;
  item_type?: 'moment' | 'comment' | string;
  author_id_hash?: string;
  is_anonymous?: boolean;
  has_media?: boolean;
  content_length_bucket?: 's' | 'm' | 'l';
  mood_tag?: string;
  time_bucket?: string;
  ab_variant?: string;
  is_first_time?: boolean;
  level?: number;
  member_status?: 'guest' | 'logged_in';
};

type EventMap = {
  view_moment: {
    item_id: string;
    mood_tag?: string;
    has_media?: boolean;
    content_length_bucket?: 's' | 'm' | 'l';
  };
  give_empathy: { item_id: string; source?: 'feed' | 'detail' };
  open_composer: { entry?: 'nav' | 'cta' | 'reminder'; composer?: 'moment' | 'comment' | 'extra' };
  publish_moment: {
    item_id: string;
    has_media?: boolean;
    content_length_bucket?: 's' | 'm' | 'l';
    mood_tag?: string;
  };
  submit_comment: { item_id: string; length_bucket?: 's' | 'm' | 'l' };
  dwell_start: {
    item_type?: 'moment' | 'comment';
    surface?: 'composer' | 'feed' | 'detail' | string;
    item_id?: string;
  };
  dwell_end: {
    item_type?: 'moment' | 'comment';
    surface?: 'composer' | 'feed' | 'detail' | string;
    item_id?: string;
    dwell_seconds: number;
  };
  scroll_depth: {
    percent_bucket: '0' | '25' | '50' | '75' | '100';
    screen_height?: number;
    doc_height?: number;
  };
  abandon_composer: {
    stage?: 'typed' | 'attached_media' | 'before_submit';
    composer?: 'moment' | 'comment' | 'extra';
    has_media?: boolean;
    content_length_bucket?: 's' | 'm' | 'l';
    mood_tag?: string;
  };
  click_navigation: {
    destination: 'today_moment' | 'today_comment' | 'collection' | string;
    source?: 'navbar' | 'nav_bar' | 'success_page' | string;
  };

  click_auth: {
    device: 'desktop' | 'mobile';
  };

  click_cta: {
    target: string;
    cta_type?: 'primary' | 'secondary' | string;
  };
};

const isProd =
  process.env.NODE_ENV === 'production' && window.location.hostname === 'connectingmoment.com';

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
  if (!isProd) {
    if (process.env.NODE_ENV !== 'production') {
      console.debug('[GA][dev-only]', eventName, { ...getCommonParams(), ...params });
    }
    return;
  }
  ReactGA.event(eventName as string, { ...getCommonParams(), ...params });
}
