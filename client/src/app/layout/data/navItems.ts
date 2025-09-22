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

export const LEVEL_MAP = {
  ASTEROID_WHITE: '/images/firstAsteroid.webp',
  ASTEROID_YELLOW: '/images/secondAsteroid.webp',
  ASTEROID_SKY: '/images/thirdAsteroid.webp',

  METEOR_WHITE: '/images/firstMeteor.webp',
  METEOR_YELLOW: '/images/secondMeteor.webp',
  METEOR_SKY: '/images/thirdMeteor.webp',

  COMET_WHITE: '/images/firstComa.webp',
  COMET_YELLOW: '/images/secondComa.webp',
  COMET_SKY: '/images/thirdComa.webp',

  ROCKY_PLANET_WHITE: '/images/firstPlanet.webp',
  ROCKY_PLANET_YELLOW: '/images/secondPlanet.webp',
  ROCKY_PLANET_SKY: '/images/thirdPlanet.webp',

  GAS_GIANT_WHITE: '/images/firstBigPlanet.webp',
  GAS_GIANT_YELLOW: '/images/secondBigPlanet.webp',
  GAS_GIANT_SKY: '/images/thirdBigPlanet.webp',
};
