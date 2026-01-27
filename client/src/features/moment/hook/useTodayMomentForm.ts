import { ROUTES } from '@/app/routes/routes';
import { checkProfanityWord } from '@/shared/types/checkProfanityWord';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';
import { useToast } from '@/shared/hooks/useToast';
import { useNavigate, useParams } from 'react-router';

export interface UseTodayMomentFormProps {
  content: string;
  handleSendContent: () => void;
}

export const useTodayMomentForm = ({ content, handleSendContent }: UseTodayMomentFormProps) => {
  const navigate = useNavigate();
  const { groupId } = useParams<{ groupId: string }>();
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();
  const { showError, showWarning } = useToast();

  const handleNavigateToTodayMomentSuccess = () => {
    if (checkProfanityWord(content)) {
      showError('모멘트에 부적절한 단어가 포함되어 있습니다.');
      return;
    }

    handleSendContent();
    if (groupId) {
      navigate(ROUTES.TODAY_MOMENT_SUCCESS.replace(':groupId', groupId));
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleNavigateToTodayMomentSuccess();
  };

  const handleTextAreaFocus = (e: React.FocusEvent<HTMLTextAreaElement>) => {
    if (!isLoggedIn) {
      e.preventDefault();
      e.target.blur();
      showWarning('Moment에 오신 걸 환영해요! 로그인하고 시작해보세요 💫');
      navigate(ROUTES.LOGIN);
      return;
    }
  };

  const MAX_LENGTH = 200;

  return {
    handleFormSubmit,
    handleNavigateToTodayMomentSuccess,
    handleTextAreaFocus,
    MAX_LENGTH,
  };
};
