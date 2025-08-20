import { NewPasswordForm } from '@/features/auth/ui/NewPasswordForm';
import styled from '@emotion/styled';

export default function NewPasswordPage() {
  return (
    <NewPassWordPageWrapper>
      <NewPasswordForm />
    </NewPassWordPageWrapper>
  );
}

const NewPassWordPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
`;
