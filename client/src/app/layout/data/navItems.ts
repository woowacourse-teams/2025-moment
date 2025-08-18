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
  ASTEROID_WHITE: '/images/firstAsteroid.png',
  ASTEROID_YELLOW: '/images/secondAsteroid.png',
  ASTEROID_SKY: '/images/thirdAsteroid.png',

  METEOR_WHITE: '/images/firstMeteor.png',
  METEOR_YELLOW: '/images/secondMeteor.png',
  METEOR_SKY: '/images/thirdMeteor.png',

  COMET_WHITE: '/images/firstComet.png',
  COMET_YELLOW: '/images/secondComet.png',
  COMET_SKY: '/images/thirdComet.png',

  ROCKY_PLANET_WHITE: '/images/firstPlanet.png',
  ROCKY_PLANET_YELLOW: '/images/secondPlanet.png',
  ROCKY_PLANET_SKY: '/images/thirdPlanet.png',

  GAS_GIANT_WHITE: '/images/firstBigPlanet.png',
  GAS_GIANT_YELLOW: '/images/secondBigPlanet.png',
  GAS_GIANT_SKY: '/images/thirdBigPlanet.png',
};
