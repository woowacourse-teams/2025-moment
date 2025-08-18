import { ROUTES } from '@/app/routes/routes';

export const navItems = [
  {
    label: '오늘의 모멘트',
    href: ROUTES.TODAY_MOMENT,
  },
  {
    label: '오늘의 코멘트',
    href: ROUTES.TODAY_COMMENT,
  },
  {
    label: '나만의 모음집',
    href: ROUTES.COLLECTION_MYMOMENT,
  },
];

export const levelMap = {
  METEOR: '/meteor.png',
  ASTEROID: '/asteroid.png',
  COMET: '/comet.png',
};
