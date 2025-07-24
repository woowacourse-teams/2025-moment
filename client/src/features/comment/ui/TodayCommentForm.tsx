import { Card } from '@/shared/ui';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { useSend } from '../hooks/useSend';

export function TodayCommentForm() {
  const { commentsData, handleChange, handleSubmit } = useSend();

  const handleSuccess = () => {
    // 성공 후 처리 로직 (예: 페이지 이동, 알림 표시 등)
    console.log('코멘트가 성공적으로 전송되었습니다.');
  };

  return (
    <Card width="medium">
      <TodayCommentWriteContent
        commentsData={commentsData}
        handleChange={handleChange}
        handleSubmit={handleSubmit}
        onSubmit={handleSuccess}
      />
    </Card>
  );
}
