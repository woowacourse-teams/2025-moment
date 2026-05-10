import styled from '@emotion/styled';
import { Button } from '@/shared/design-system/button';

export const TodayContentForm = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

export const TagWrapper = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
`;

export const TagLabel = styled.div`
  font-size: 1.2rem;
`;

export const FloatingSubmitWrapper = styled.div`
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 48px);
  max-width: 480px;
  z-index: 100;
`;

export const FloatingSubmitButton = styled(Button)`
  width: 100%;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
`;
