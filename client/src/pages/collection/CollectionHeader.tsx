import { ROUTES } from '@/app/routes/routes';
import { useLocation } from 'react-router';
import * as S from './index.styles';

export const CollectionHeader = () => {
  const currentpath = useLocation().pathname;

  console.log('currentpath', currentpath);

  return (
    <S.CollectionHeaderContainer>
      <S.CollectionHeaderLinkContainer
        to={ROUTES.COLLECTION_MYMOMENT}
        className={currentpath === ROUTES.COLLECTION_MYMOMENT ? 'active' : ''}
      >
        나의 모멘트 모음집
      </S.CollectionHeaderLinkContainer>
      <S.CollectionHeaderLinkContainer
        to={ROUTES.COLLECTION_MYCOMMENT}
        className={currentpath === ROUTES.COLLECTION_MYCOMMENT ? 'active' : ''}
      >
        나의 코멘트 모음집
      </S.CollectionHeaderLinkContainer>
    </S.CollectionHeaderContainer>
  );
};
