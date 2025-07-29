import { ToastProvider } from '@/shared/context/toast/ToastProvider';
import { useToast } from '@/shared/hooks/useToast';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import React from 'react';

const meta: Meta = {
  title: 'Shared/Toast',
  decorators: [
    Story => (
      <ToastProvider>
        <Story />
      </ToastProvider>
    ),
  ],
  parameters: {
    layout: 'centered',
  },
};

export default meta;

const ToastExample: React.FC = () => {
  const { showSuccess, showError } = useToast();

  return (
    <div style={{ display: 'flex', gap: '16px', flexDirection: 'column' }}>
      <button
        onClick={() => showSuccess('성공적으로 처리되었습니다!')}
        style={{
          padding: '12px 24px',
          backgroundColor: '#10B981',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
        }}
      >
        성공 메시지 보기
      </button>

      <button
        onClick={() => showError('오류가 발생했습니다. 다시 시도해주세요.')}
        style={{
          padding: '12px 24px',
          backgroundColor: '#EF4444',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
        }}
      >
        에러 메시지 보기
      </button>

      <button
        onClick={() => {
          showSuccess('첫 번째 메시지');
          setTimeout(() => showError('두 번째 메시지'), 500);
          setTimeout(() => showSuccess('세 번째 메시지'), 1000);
        }}
        style={{
          padding: '12px 24px',
          backgroundColor: '#6366F1',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
        }}
      >
        연속 메시지 (교체됨)
      </button>

      <button
        onClick={() => showSuccess('이 메시지는 10초 후에 사라집니다', 10000)}
        style={{
          padding: '12px 24px',
          backgroundColor: '#F59E0B',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
        }}
      >
        긴 지속 시간 메시지
      </button>
    </div>
  );
};

export const Default: StoryObj = {
  render: () => <ToastExample />,
};
