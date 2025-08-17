import { mockProfile } from '@/features/my/api/mockData';
import * as S from './index.styles';
import { Button, Card } from '@/shared/ui';
import { EXPBar } from '@/widgets/EXPBar/EXPBar';
import { levelMap } from '@/app/layout/data/navItems';
import { Modal } from '@/shared/ui/modal/Modal';
import { LevelTable } from '@/widgets/levelTable/LevelTable';
import { useState } from 'react';

export default function MyPage() {
  const [isOpen, setIsOpen] = useState(false);
  const EXPBarProgress =
    (mockProfile.data.expStar / (mockProfile.data.nextStepExp + mockProfile.data.expStar)) * 100;

  const totalExp = mockProfile.data.nextStepExp + mockProfile.data.expStar;

  return (
    <S.MyPageWrapper>
      <S.UserInfoSection>
        <S.SectionTitle>내 정보</S.SectionTitle>
        <Card width="large">
          <S.UserInfoContainer>
            <S.UserProfileSection>
              <S.UserBasicInfo>
                <S.Email>{mockProfile.data.email}</S.Email>
                <S.UserInfo>
                  <p>{mockProfile.data.nickname}</p>
                  <span>•</span>
                  <S.LevelBadge>{mockProfile.data.level}</S.LevelBadge>
                  <S.LevelIcon
                    src={levelMap[mockProfile.data.level as keyof typeof levelMap]}
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
                  <span className="current">{mockProfile.data.expStar}</span>
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

      <S.StatusSection>
        <S.SectionTitle>내 상태</S.SectionTitle>
        <Card width="large">
          <S.UserInfoContainer>
            <p>내 상태</p>
          </S.UserInfoContainer>
        </Card>
      </S.StatusSection>

      <S.Divider />

      <S.SettingSection>
        <S.SectionTitle>설정</S.SectionTitle>
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
