import { FindPasswordForm } from '@/features/auth/ui/FindPasswordForm';
import styled from '@emotion/styled';

export default function FindPasswordPage() {
  return (
    <FindPasswordPageWrapper>
      <FindPasswordForm />
    </FindPasswordPageWrapper>
  );
}

const FindPasswordPageWrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
`;
