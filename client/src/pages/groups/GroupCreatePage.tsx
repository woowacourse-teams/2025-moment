import { GroupCreateForm } from '@/features/group/ui/GroupCreateForm';
import { useNavigate } from 'react-router';
import { ROUTES } from '@/app/routes/routes';
import * as S from './GroupCreatePage.styles';

export default function GroupCreatePage() {
  const navigate = useNavigate();

  const handleSuccess = () => {
    navigate(ROUTES.GROUPS);
  };

  const handleCancel = () => {
    navigate(ROUTES.GROUPS);
  };

  return (
    <S.PageContainer>
      <S.Header>
        <S.Title>새 그룹 만들기</S.Title>
        <S.Subtitle>친구들과 함께할 그룹을 만들어보세요</S.Subtitle>
      </S.Header>
      <GroupCreateForm onSuccess={handleSuccess} onCancel={handleCancel} />
    </S.PageContainer>
  );
}
