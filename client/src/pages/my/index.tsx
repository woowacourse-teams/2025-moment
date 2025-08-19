import { LEVEL_MAP } from '@/app/layout/data/navItems';
import { useProfileQuery } from '@/features/my/hooks/useProfileQuery';
import { useRewardHistoryQuery } from '@/features/my/hooks/useRewardHistory';
import { RewardHistoryPagination } from '@/features/my/ui/RewardHistoryPagination';
import { RewardHistoryTable } from '@/features/my/ui/RewardHistoryTable';
import { Button, Card, NotFound } from '@/shared/ui';
import { Modal } from '@/shared/ui/modal/Modal';
import { EXPBar } from '@/widgets/EXPBar/EXPBar';
import { LevelTable } from '@/widgets/levelTable/LevelTable';
import { AlertCircle } from 'lucide-react';
import { useState } from 'react';
import * as S from './index.styles';
import { ChangeNicknameForm } from '@/features/my/ui/ChangeNicknameForm';
import { ChangePasswordForm } from '@/features/my/ui/ChangePasswordForm';
import { useModal } from '@/shared/hooks/useModal';

export const DEFAULT_PAGE_SIZE = 10;

export default function MyPage() {
  const {
    isOpen: isLevelOpen,
    handleOpen: handleOpenLevelModal,
    handleClose: handleCloseLevelModal,
  } = useModal();
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
  const [currentPage, setCurrentPage] = useState(0);
  const [localNickname, setLocalNickname] = useState('');
  const { data: myProfile, isLoading: isProfileLoading, error: profileError } = useProfileQuery();

  const {
    data: rewardHistory,
    isLoading,
    error,
  } = useRewardHistoryQuery({
    pageNum: currentPage,
    pageSize: DEFAULT_PAGE_SIZE,
  });

  if (isProfileLoading) return <div>프로필 로딩 중...</div>;
  if (profileError) return <div>프로필을 불러올 수 없습니다.</div>;
  if (!myProfile) return <div>프로필 데이터가 없습니다.</div>;

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleNicknameChange = (nickname: string) => {
    setLocalNickname(nickname);
  };

  const EXPBarProgress = (myProfile.expStar / myProfile.nextStepExp) * 100;

  const totalExp = myProfile.nextStepExp;

  return (
    <S.MyPageWrapper>
      <S.UserInfoSection>
        <S.SectionTitle>내 정보</S.SectionTitle>
        <Card width="large">
          <S.UserInfoContainer>
            <S.UserProfileSection>
              <S.UserBasicInfo>
                <S.Email>{myProfile.email}</S.Email>
                <S.UserInfo>
                  <p>{myProfile.nickname}</p>

                  <Button variant="primary" title="닉네임 변경" onClick={handleOpenNicknameModal} />
                  {myProfile.loginType === 'EMAIL' && (
                    <Button
                      variant="primary"
                      title="비밀번호 변경"
                      onClick={handleOpenPasswordModal}
                    />
                  )}
                </S.UserInfo>
              </S.UserBasicInfo>
            </S.UserProfileSection>

            <S.EXPSection>
              <S.EXPLabel>경험치</S.EXPLabel>
              <S.EXPBarContainer>
                <EXPBar progress={EXPBarProgress} />
                <S.EXPStats>
                  <span className="current">{myProfile.expStar}</span>
                  <span className="separator">/</span>
                  <span className="total">{totalExp}</span>
                </S.EXPStats>
                <S.LevelBadge>{myProfile.level}</S.LevelBadge>
                <S.LevelIcon
                  src={LEVEL_MAP[myProfile.level as keyof typeof LEVEL_MAP]}
                  alt="레벨 등급표"
                />
                <Button variant="primary" title="레벨 등급표" onClick={handleOpenLevelModal} />
              </S.EXPBarContainer>
            </S.EXPSection>
          </S.UserInfoContainer>
        </Card>
      </S.UserInfoSection>

      <S.RewardHistorySection>
        <S.SectionTitle>별조각 이력</S.SectionTitle>
        <Card width="large">
          <S.RewardHistoryContainer>
            {isLoading ? (
              <p>로딩 중 입니다</p>
            ) : error ? (
              <NotFound
                title="데이터를 불러올 수 없습니다"
                subtitle="잠시 후 다시 시도해주세요"
                icon={AlertCircle}
                size="large"
              />
            ) : rewardHistory ? (
              <>
                <RewardHistoryTable items={rewardHistory.items} />
                <RewardHistoryPagination
                  currentPage={rewardHistory.currentPageNum}
                  totalPages={rewardHistory.totalPages}
                  onPageChange={handlePageChange}
                />
              </>
            ) : (
              <p>데이터가 없습니다.</p>
            )}
          </S.RewardHistoryContainer>
        </Card>
      </S.RewardHistorySection>

      <S.Divider />

      <Modal isOpen={isLevelOpen} position="center" size="large" onClose={handleCloseLevelModal}>
        <Modal.Header title="레벨 등급표" />
        <Modal.Content>
          <LevelTable />
        </Modal.Content>
      </Modal>

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
        <Modal.Header title={myProfile.expStar < 100 ? '<별조각 보유 부족>' : '<닉네임 변경>'} />
        <Modal.Content>
          {myProfile.expStar < 100 ? (
            <p>별조각 100개 이상 보유 시 닉네임 변경이 가능합니다.</p>
          ) : (
            <ChangeNicknameForm nickname={localNickname} updateNickname={handleNicknameChange} />
          )}
        </Modal.Content>
      </Modal>
    </S.MyPageWrapper>
  );
}
