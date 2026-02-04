import { useProfileQuery } from '@/features/my/api/useProfileQuery';
import { ChangeNicknameForm } from '@/features/my/ui/ChangeNicknameForm';
import { ChangePasswordForm } from '@/features/my/ui/ChangePasswordForm';
import { useModal } from '@/shared/design-system/modal';
import { Button } from '@/shared/design-system/button/Button';
import { Card } from '@/shared/design-system/card/Card';
import { Modal } from '@/shared/design-system/modal/Modal';
import { MyGroupList } from '@/features/group/ui/MyGroupList';
import { useState } from 'react';
import * as S from './index.styles';
import { useLogoutMutation } from '@/features/auth/api/useLogoutMutation';
import { useDeleteAccountMutation } from '@/features/auth/api/useDeleteAccountMutation';
import { truncateEmail } from '@/shared/utils/email';

export const DEFAULT_PAGE_SIZE = 10;

export default function MyPage() {
  const {
    isOpen: isPasswordOpen,
    handleOpen: handleOpenPasswordModal,
    handleClose: handleClosePasswordModal,
  } = useModal();
  const {
    isOpen: isNicknameOpen,
    handleOpen: handleOpenNicknameModal,
    handleClose: handleCloseNicknameModal,
  } = useModal();
  const {
    isOpen: isDeleteAccountOpen,
    handleOpen: handleOpenDeleteAccountModal,
    handleClose: handleCloseDeleteAccountModal,
  } = useModal();
  const [localNickname, setLocalNickname] = useState('');
  const { data: myProfile, isLoading: isProfileLoading, error: profileError } = useProfileQuery();
  const logoutMutation = useLogoutMutation();
  const deleteAccountMutation = useDeleteAccountMutation();

  if (isProfileLoading) return <div>프로필 로딩 중...</div>;
  if (profileError) return <div>프로필을 불러올 수 없습니다.</div>;
  if (!myProfile) return <div>프로필 데이터가 없습니다.</div>;

  const handleNicknameChange = (nickname: string) => {
    setLocalNickname(nickname);
  };

  return (
    <S.MyPageWrapper>
      <S.UserInfoSection>
        <S.SectionTitleContainer>
          <S.SectionTitle>내 정보</S.SectionTitle>
        </S.SectionTitleContainer>
        <Card width="large">
          <S.UserInfoContainer>
            <S.UserProfileSection>
              <S.Email>{truncateEmail(myProfile.email)}</S.Email>
              <S.UserInfo>
                <p>{myProfile.nickname}</p>
                <S.ButtonContainer>
                  <Button variant="primary" title="닉네임 변경" onClick={handleOpenNicknameModal} />
                  {myProfile.loginType === 'EMAIL' && (
                    <Button
                      variant="primary"
                      title="비밀번호 변경"
                      onClick={handleOpenPasswordModal}
                    />
                  )}
                  <Button
                    variant="primary"
                    title="로그아웃"
                    onClick={() => logoutMutation.mutate()}
                  />
                  <Button
                    variant="danger"
                    title="회원 탈퇴"
                    onClick={handleOpenDeleteAccountModal}
                  />
                </S.ButtonContainer>
              </S.UserInfo>
            </S.UserProfileSection>
          </S.UserInfoContainer>
        </Card>
      </S.UserInfoSection>

      <S.GroupSection>
        <S.SectionTitleContainer>
          <S.SectionTitle>그룹 관리</S.SectionTitle>
        </S.SectionTitleContainer>
        <Card width="large">
          <MyGroupList />
        </Card>
      </S.GroupSection>

      <S.Divider />

      <Modal
        isOpen={isPasswordOpen}
        position="center"
        size="medium"
        onClose={handleClosePasswordModal}
      >
        <Modal.Header title="비밀번호 변경" />
        <Modal.Content>
          <ChangePasswordForm />
        </Modal.Content>
      </Modal>

      <Modal
        isOpen={isNicknameOpen}
        position="center"
        size="small"
        onClose={handleCloseNicknameModal}
      >
        <Modal.Header title="<닉네임 변경>" />
        <Modal.Content>
          <ChangeNicknameForm
            nickname={localNickname}
            updateNickname={handleNicknameChange}
            handleCloseNicknameModal={handleCloseNicknameModal}
          />
        </Modal.Content>
      </Modal>

      <Modal
        isOpen={isDeleteAccountOpen}
        position="center"
        size="small"
        onClose={handleCloseDeleteAccountModal}
      >
        <Modal.Header title="회원 탈퇴" />
        <Modal.Content>
          <S.DeleteAccountContent>
            <p>정말로 탈퇴하시겠습니까?</p>
            <p>탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.</p>
            <S.DeleteAccountButtonContainer>
              <Button variant="secondary" title="취소" onClick={handleCloseDeleteAccountModal} />
              <Button
                variant="danger"
                title="탈퇴하기"
                onClick={() => deleteAccountMutation.mutate()}
                disabled={deleteAccountMutation.isPending}
              />
            </S.DeleteAccountButtonContainer>
          </S.DeleteAccountContent>
        </Modal.Content>
      </Modal>
    </S.MyPageWrapper>
  );
}
