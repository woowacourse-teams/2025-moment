import { useChangePassword } from '@/features/auth/hooks/useChangePassword';
import { Button } from '@/shared/ui';
import * as S from './ChangePasswordForm.styles';

export const ChangePasswordForm = () => {
  const { changePasswordData, errors, handleChange, handleSubmit, isSubmitDisabled } =
    useChangePassword();
  return (
    <S.ChangePassWordFormWrapper>
      <S.ChangePasswordFormContent>
        <S.ChangePasswordFormInput
          type="password"
          placeholder="새 비밀번호"
          value={changePasswordData.newPassword}
          onChange={handleChange('newPassword')}
        />
        <S.ErrorMessage>{errors.newPassword || ''}</S.ErrorMessage>
        <S.ChangePasswordFormInput
          type="password"
          placeholder="새 비밀번호 확인"
          value={changePasswordData.checkedPassword}
          onChange={handleChange('checkedPassword')}
        />
        <S.ErrorMessage>{errors.checkedPassword || ''}</S.ErrorMessage>
      </S.ChangePasswordFormContent>
      <Button
        title="변경하기"
        variant="primary"
        onClick={handleSubmit}
        disabled={isSubmitDisabled}
      />
    </S.ChangePassWordFormWrapper>
  );
};
