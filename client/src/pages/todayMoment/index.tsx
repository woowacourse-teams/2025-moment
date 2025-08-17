import { useMomentWritingStatusQuery } from '@/features/moment/hook/useMomentWritingStatusQuery';
import { TodayMomentForm } from '@/features/moment/ui/TodayMomentForm';
import { TitleContainer } from '@/shared/ui/titleContainer/TitleContainer';
import styled from '@emotion/styled';
import { useNavigate } from 'react-router';
import { useEffect } from 'react';
import { ROUTES } from '@/app/routes/routes';

export default function TodayMomentPage() {
  const { data: momentWritingStatusData } = useMomentWritingStatusQuery();
  const navigate = useNavigate();

  const momentBasicWritable = momentWritingStatusData?.data?.status;

  useEffect(() => {
    if (momentBasicWritable === 'DENIED') {
      navigate(ROUTES.TODAY_MOMENT_SUCCESS, { replace: true });
    }
  }, [momentBasicWritable]);

  return (
    <TodayPageWrapper>
      <TitleContainer
        title="오늘의 모멘트"
        subtitle="하루에 한 번, 당신의 특별한 모멘트를 공유해보세요"
      />
      <TodayMomentForm />
    </TodayPageWrapper>
  );
}

export const TodayPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
`;
