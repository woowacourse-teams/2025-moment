import { ROUTES } from '@/app/routes/routes';
import { Button } from '@/shared/ui/button/Button';
import { useNavigate } from 'react-router';
import * as S from './index.styles';

const NotFoundPage = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate(ROUTES.ROOT);
  };

  return (
    <S.NotFoundContainer>
      <S.NotFoundContent>
        <S.ErrorCode>404</S.ErrorCode>
        <S.ErrorTitle>페이지를 찾을 수 없습니다</S.ErrorTitle>
        <S.ErrorDescription>
          죄송합니다. 요청하신 페이지를 찾을 수 없습니다.
          <br />
          주소를 다시 확인해주세요.
        </S.ErrorDescription>
        <S.ButtonContainer>
          <Button onClick={handleGoHome} variant="primary" title="홈으로 돌아가기" />
        </S.ButtonContainer>
      </S.NotFoundContent>
    </S.NotFoundContainer>
  );
};

export default NotFoundPage;
