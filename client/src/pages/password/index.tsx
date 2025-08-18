import { Input } from '@/shared/ui/input/Input';
import * as S from './index.styles';
import { Button, Card } from '@/shared/ui';
import { useChangePassword } from '@/features/auth/hooks/useChangePassword';

export default function PasswordPage() {
  const { changePasswordData, errors, handleChange, handleSubmit, isSubmitDisabled } =
    useChangePassword();
  return (
    <S.PasswordPageWrapper>
      <S.PasswordPageTitle>
        <p>비밀번호 변경</p>
      </S.PasswordPageTitle>
      <Card width="medium">
        <S.PasswordPageContent>
          <Input
            type="password"
            placeholder="새 비밀번호"
            value={changePasswordData.newPassword}
            onChange={handleChange('newPassword')}
          />
          <S.ErrorMessage>{errors.newPassword || ''}</S.ErrorMessage>
          <Input
            type="password"
            placeholder="새 비밀번호 확인"
            value={changePasswordData.checkPassword}
            onChange={handleChange('checkPassword')}
          />
          <S.ErrorMessage>{errors.checkPassword || ''}</S.ErrorMessage>
        </S.PasswordPageContent>
        <Button
          title="변경하기"
          variant="primary"
          onClick={handleSubmit}
          disabled={isSubmitDisabled}
        />
      </Card>
    </S.PasswordPageWrapper>
  );
}
