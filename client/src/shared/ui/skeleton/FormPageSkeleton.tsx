import { Skeleton } from '@/shared/design-system/skeleton';
import styled from '@emotion/styled';
import React from 'react';

const Wrapper = styled.main`
  width: 100%;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
`;

const FormBox = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 400px;
  padding: 32px 24px;
`;

export const FormPageSkeleton: React.FC = () => {
  return (
    <Wrapper>
      <FormBox>
        <Skeleton width="50%" height="28px" borderRadius="6px" />
        <Skeleton width="100%" height="48px" borderRadius="8px" />
        <Skeleton width="100%" height="48px" borderRadius="8px" />
        <Skeleton width="100%" height="48px" borderRadius="8px" />
        <Skeleton width="100%" height="48px" borderRadius="24px" />
      </FormBox>
    </Wrapper>
  );
};
