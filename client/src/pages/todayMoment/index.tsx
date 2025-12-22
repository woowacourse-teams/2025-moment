import { ROUTES } from '@/app/routes/routes';
import { useMomentWritingStatusQuery } from '@/features/moment/api/useMomentWritingStatusQuery';
import { useSendMoments } from '@/features/moment/hook/useSendMoments';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/design-system/titleContainer/TitleContainer';
import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router';
import * as S from './index.styles';
import { track } from '@/shared/lib/ga/track';
import { useDwell } from '@/shared/lib/ga/hooks/useDwell';

export default function TodayMomentPage() {
  const {
    handleContentChange,
    handleImageChange,
    handleTagNameClick,
    handleSendContent,
    content,
    tagNames,
  } = useSendMoments();
  const { data: momentWritingStatusData } = useMomentWritingStatusQuery();
  const momentBasicWritable = momentWritingStatusData?.data?.status;
  const navigate = useNavigate();

  const location = useLocation();
  useEffect(() => {
    track('open_composer', {
      entry: location.state?.entry ?? 'nav',
      composer: 'moment',
    });
  }, []);

  useDwell({ item_type: 'moment', surface: 'composer' });

  useEffect(() => {
    if (momentBasicWritable === 'DENIED') {
      navigate(ROUTES.TODAY_MOMENT_SUCCESS, { replace: true });
    }
  }, [momentBasicWritable]);

  return (
    <S.TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayMomentForm
        handleContentChange={handleContentChange}
        handleImageChange={handleImageChange}
        handleTagNameClick={handleTagNameClick}
        handleSendContent={handleSendContent}
        content={content}
        tagNames={tagNames}
      />
    </S.TodayPageWrapper>
  );
}
