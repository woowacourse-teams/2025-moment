import * as S from './index.styles';
import { Button, Card, NotFound } from '@/shared/ui';
import { EXPBar } from '@/widgets/EXPBar/EXPBar';
import { levelMap } from '@/app/layout/data/navItems';
import { Modal } from '@/shared/ui/modal/Modal';
import { LevelTable } from '@/widgets/levelTable/LevelTable';
import { useState } from 'react';
import { useRewardHistoryQuery } from '@/features/my/hooks/useRewardHistory';
import { RewardHistoryTable } from '@/features/my/ui/RewardHistoryTable';
import { RewardHistoryPagination } from '@/features/my/ui/RewardHistoryPagination';
import { AlertCircle } from 'lucide-react';
import { Link } from 'react-router';
import { ROUTES } from '@/app/routes/routes';
import { useProfileQuery } from '@/features/my/hooks/useProfileQuery';

export default function MyPage() {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isOpen, setIsOpen] = useState(false);

  const { data: myProfile, isLoading: isProfileLoading, error: profileError } = useProfileQuery();

  if (isProfileLoading) return <div>프로필 로딩 중...</div>;
  if (profileError) return <div>프로필을 불러올 수 없습니다.</div>;
  if (!myProfile) return <div>프로필 데이터가 없습니다.</div>;

  const {
    data: rewardHistory,
    isLoading,
    error,
  } = useRewardHistoryQuery({
    pageNum: currentPage,
    pageSize,
  });

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const EXPBarProgress = (myProfile.expStar / (myProfile.nextStepExp + myProfile.expStar)) * 100;

  const totalExp = myProfile.nextStepExp + myProfile.expStar;

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
                  <span>•</span>
                  <S.LevelBadge>{myProfile.level}</S.LevelBadge>
                  <S.LevelIcon
                    src={levelMap[myProfile.level as keyof typeof levelMap]}
                    alt="레벨 등급표"
                  />
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
                <Button variant="primary" title="레벨 등급표" onClick={() => setIsOpen(true)} />
              </S.EXPBarContainer>
            </S.EXPSection>
          </S.UserInfoContainer>
        </Card>
      </S.UserInfoSection>

      <S.Divider />

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

      <S.SettingSection>
        <S.SectionTitle>설정</S.SectionTitle>
        <p>
          <Link to={ROUTES.PASSWORD}>비밀번호 변경</Link>
        </p>
      </S.SettingSection>

      <Modal isOpen={isOpen} position="center" size="large" onClose={() => setIsOpen(false)}>
        <Modal.Header title="레벨 등급표" />
        <Modal.Content>
          <LevelTable />
        </Modal.Content>
      </Modal>
    </S.MyPageWrapper>
  );
}
