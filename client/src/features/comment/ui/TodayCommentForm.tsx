import { useSubmitted } from '@/features/moment/hook/useSubmitted';
import { Card } from '@/shared/ui';
import { TodayCommentSuccessContent } from './TodayCommentSuccessContent';
import { TodayCommentWriteContent } from './TodayCommentWriteContent';
import { useNavigate } from 'react-router';

export function TodayCommentForm() {
  const { isSubmitted, handleSubmit } = useSubmitted();

  const navigate = useNavigate();

  const handlePagination = () => navigate('/post-comments');

  return (
    <Card width="medium">
      {!isSubmitted ? (
        <TodayCommentWriteContent onSubmit={handleSubmit} />
      ) : (
        <TodayCommentSuccessContent onPagination={handlePagination} />
      )}
    </Card>
  );
}
