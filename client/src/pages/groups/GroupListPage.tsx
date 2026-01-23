import { Button } from '@/shared/design-system/button/Button';
import { GroupList } from '@/features/group/ui/GroupList';
import { useNavigate } from 'react-router';
import { ROUTES } from '@/app/routes/routes';
import * as S from './GroupListPage.styles';

export default function GroupListPage() {
  const navigate = useNavigate();

  const handleCreateGroup = () => {
    navigate(ROUTES.GROUP_CREATE);
  };

  return (
    <S.PageContainer>
      <S.Header>
        <S.Title>내 그룹</S.Title>
        <Button title="새 그룹 만들기" variant="primary" onClick={handleCreateGroup} />
      </S.Header>
      <GroupList />
    </S.PageContainer>
  );
}
