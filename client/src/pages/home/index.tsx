import { ROUTES } from '@/app/routes/routes';
import { useDelayedVisible } from '@/shared/hooks/useDelayedVisible';
import { Button } from '@/shared/design-system/button/Button';
import { Hero } from '@/widgets/hero';
import { useNavigate } from 'react-router';
import * as S from './index.styles';
import { useScrollAnimation } from '@/shared/hooks/useScrollAnimation';
import { PropsWithChildren, useState } from 'react';
import { explainData } from './const';
import { useScrollDepth } from '@/shared/lib/ga/hooks/useScrollDepth';
import { track } from '@/shared/lib/ga/track';
import { Modal } from '@/shared/design-system/modal/Modal';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useGroupsQuery } from '@/features/group/api/useGroupsQuery';
import { GroupList } from '@/features/group/ui/GroupList';
import { GroupCreateForm } from '@/features/group/ui/GroupCreateForm';
import { GroupJoinForm } from '@/features/group/ui/GroupJoinForm';
import { GroupCreateSuccess } from '@/features/group/ui/GroupCreateSuccess';

export default function HomePage() {
  useScrollDepth();

  const navigate = useNavigate();
  const { isVisible } = useDelayedVisible({ delay: 100 });
  const { data: isLoggedInQuery } = useCheckIfLoggedInQuery();
  const isLoggedIn = !!isLoggedInQuery;

  const { data: groupsResponse, refetch: refetchGroups } = useGroupsQuery({ enabled: isLoggedIn });

  const [modalType, setModalType] = useState<'none' | 'create' | 'join' | 'invite' | 'join-success'>('none');
  const [createdGroupInfo, setCreatedGroupInfo] = useState<{
    groupId: number;
    code: string;
  } | null>(null);

  const groups = groupsResponse?.data || [];
  const hasGroups = isLoggedIn && groups.length > 0;

  const handleClick = () => {
    track('click_cta', { cta_type: 'primary' });
    navigate(ROUTES.LOGIN);
  };

  const handleCreateGroupCtx = () => setModalType('create');
  const handleJoinGroupCtx = () => setModalType('join');
  const handleCloseModal = () => setModalType('none');

  const handleJoinSuccess = async () => {
    await refetchGroups();
    setModalType('join-success');
  };

  const handleCreateSuccess = async (groupId: number, code: string) => {
    await refetchGroups();
    setCreatedGroupInfo({ groupId, code });
    setModalType('invite');
  };

  return (
    <>
      <S.HomePageWrapper>
        <S.MainContainer>
          <S.HeroSection>
            <Hero />
          </S.HeroSection>

          <S.ContentSection $isVisible={isVisible}>
            {isLoggedIn ? (
              hasGroups ? (
                <S.GroupListContainer>
                  <GroupList />
                  <S.ActionButtons>
                    <Button title="그룹 생성" variant="secondary" onClick={handleCreateGroupCtx} />
                    <Button title="그룹 참여" variant="secondary" onClick={handleJoinGroupCtx} />
                  </S.ActionButtons>
                </S.GroupListContainer>
              ) : (
                <S.OnboardingContainer>
                  <S.OnboardingTitle>시작하기</S.OnboardingTitle>
                  <S.OnboardingDescription>
                    그룹을 만들거나 초대 코드를 통해 참여해보세요.
                  </S.OnboardingDescription>
                  <S.OnboardingButtonGroup>
                    <S.OnboardingCard onClick={handleCreateGroupCtx}>
                      <h3>그룹 만들기</h3>
                      <p>새로운 공간을 만들어보세요</p>
                    </S.OnboardingCard>
                    <S.OnboardingCard onClick={handleJoinGroupCtx}>
                      <h3>그룹 참여하기</h3>
                      <p>초대 코드로 참여해보세요</p>
                    </S.OnboardingCard>
                  </S.OnboardingButtonGroup>
                </S.OnboardingContainer>
              )
            ) : (
              <Button title="모멘트 작성하기" variant="secondary" onClick={handleClick} />
            )}
          </S.ContentSection>

          <S.ContentSection $isVisible={isVisible}>
            <S.BottomArrow
              $isVisible={isVisible}
              webpSrc="/images/belowAirplane.webp"
              fallbackSrc="/images/fallback/belowAirplane.png"
              alt="아래로 스크롤하세요"
              role="img"
            />
          </S.ContentSection>
          <S.HighlightedTextContainer
            $isVisible={isVisible}
            role="region"
            aria-label="서비스 핵심 가치"
          >
            <S.HighlightedText>" 비교 없는 따뜻한 소통의 공간 "</S.HighlightedText>
            <S.HighlightedText>" 익명 속에서 편안하게 마음을 나눌 수 있는 곳 "</S.HighlightedText>
          </S.HighlightedTextContainer>
        </S.MainContainer>
        <AnimatedIntroSection>
          <S.IntroSection>
            <S.IntroTitleLogo
              webpSrc="/images/momentLogo.webp"
              fallbackSrc="/images/fallback/momentLogo.png"
              alt=""
            />
            <h2 id="intro-title" style={{ position: 'absolute', left: '-10000px' }}>
              moment 소개
            </h2>
            <S.IntroText>
              "모멘트(Moment)"는 사용자들이 삶의 모든 순간(Moment)을 공유하며 서로에게 따뜻한 칭찬과
              위로를 건네는 소셜 네트워크 서비스입니다. <br />
              힘든 순간, 뿌듯한 순간, 위로받고 싶은 순간, 칭찬받고 싶은 모든 순간을 짧은 기록으로
              남기고, 긍정적인 상호작용을 통해 정서적 지지와 유대감을 나눌 수 있는 공간을
              제공합니다.
            </S.IntroText>
            <S.IntroImagesWrapper>
              <S.IntroIcon
                webpSrc="/images/rocket.webp"
                fallbackSrc="/images/fallback/rocket.png"
                alt=""
              />
              <S.IntroIcon
                webpSrc="/images/paperAirplane.webp"
                fallbackSrc="/images/fallback/paperAirplane.png"
                alt=""
              />
              <S.IntroIcon
                webpSrc="/images/spaceMan.webp"
                fallbackSrc="/images/fallback/spaceMan.png"
                alt=""
              />
            </S.IntroImagesWrapper>
          </S.IntroSection>
        </AnimatedIntroSection>
        {explainData.map(section => (
          <AnimatedIntroSection key={section.title}>
            <ExplainSection {...section} />
          </AnimatedIntroSection>
        ))}
      </S.HomePageWrapper>

      <Modal isOpen={modalType === 'create'} onClose={handleCloseModal}>
        <Modal.Header title="그룹 생성" showCloseButton />
        <Modal.Content>
          <GroupCreateForm onSuccess={handleCreateSuccess} onCancel={handleCloseModal} />
        </Modal.Content>
      </Modal>

      <Modal isOpen={modalType === 'join'} onClose={handleCloseModal}>
        <Modal.Header title="그룹 참여" showCloseButton />
        <Modal.Content>
          <GroupJoinForm onSuccess={handleJoinSuccess} onCancel={handleCloseModal} />
        </Modal.Content>
      </Modal>

      <Modal isOpen={modalType === 'invite'} onClose={handleCloseModal}>
        <Modal.Header title="참여 코드" showCloseButton />
        <Modal.Content>
          {createdGroupInfo && (
            <GroupCreateSuccess
              groupId={createdGroupInfo.groupId}
              inviteCode={createdGroupInfo.code}
              onClose={handleCloseModal}
            />
          )}
        </Modal.Content>
      </Modal>

      <Modal isOpen={modalType === 'join-success'} onClose={handleCloseModal}>
        <Modal.Header title="그룹 참여 신청 완료" showCloseButton />
        <Modal.Content>
          <S.OnboardingContainer>
            <S.OnboardingDescription>
              그룹 참여 신청이 완료되었습니다!
              <br />
              방장이 승인을 허용해야 그룹에 들어갈 수 있습니다.
            </S.OnboardingDescription>
            <Button title="확인" variant="primary" onClick={handleCloseModal} />
          </S.OnboardingContainer>
        </Modal.Content>
      </Modal>
    </>
  );
}

const AnimatedIntroSection = ({ children }: PropsWithChildren) => {
  const [isVisible, setIsVisible] = useState(false);
  const ref = useScrollAnimation({
    onVisible: () => setIsVisible(true),
  });

  return (
    <S.IntroSectionWrapper ref={ref} $isVisible={isVisible}>
      {children}
    </S.IntroSectionWrapper>
  );
};

const ExplainSection = ({
  image,
  title,
  text1,
  text2,
}: {
  image: string;
  title: string;
  text1?: string;
  text2?: string;
}) => {
  const fallbackImage = image.replace('/images/', '/images/fallback/').replace('.webp', '.png');

  return (
    <S.ExplainSection>
      <S.ExplainImage webpSrc={image} fallbackSrc={fallbackImage} alt="" />
      <S.ExplainContainer>
        <S.ExplainTitle>{title}</S.ExplainTitle>
        <S.ExplainText>{text1}</S.ExplainText>
        <S.ExplainText>{text2}</S.ExplainText>
      </S.ExplainContainer>
    </S.ExplainSection>
  );
};
