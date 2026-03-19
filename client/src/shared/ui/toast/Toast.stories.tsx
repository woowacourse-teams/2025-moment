import { Toast } from './Toast';
import { toast } from '@/shared/store/toast';
import type { Meta, StoryObj } from '@storybook/react-webpack5';
import React from 'react';

const meta: Meta = {
  title: 'Shared/Toast',
  parameters: {
    layout: 'centered',
  },
};

export default meta;

const ToastExample: React.FC = () => {
  return (
    <>
      <Toast />

      <div style={{ display: 'flex', gap: '16px', flexDirection: 'column' }}>
        <button
          onClick={() => toast.success('성공적으로 처리되었습니다!')}
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
          onClick={() => toast.error('오류가 발생했습니다. 다시 시도해주세요.')}
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
            toast.success('첫 번째 메시지');
            setTimeout(() => toast.error('두 번째 메시지'), 500);
            setTimeout(() => toast.success('세 번째 메시지'), 1000);
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
          onClick={() => toast.success('이 메시지는 10초 후에 사라집니다', 10000)}
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

        <button
          onClick={() => toast.message('나의 모멘트에 코멘트가 달렸습니다!', 'moment')}
          style={{
            padding: '12px 24px',
            backgroundColor: '#8B5CF6',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          모멘트 알림 메시지 (클릭 가능)
        </button>

        <button
          onClick={() =>
            toast.message('나의 코멘트에 에코가 달렸습니다! 별조각 3개를 획득했습니다!', 'comment')
          }
          style={{
            padding: '12px 24px',
            backgroundColor: '#EC4899',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          코멘트 알림 메시지 (클릭 가능)
        </button>

        <button
          onClick={() => toast.message('일반 알림 메시지입니다')}
          style={{
            padding: '12px 24px',
            backgroundColor: '#64748B',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          일반 메시지 (클릭 불가)
        </button>

        <button
          onClick={() => {
            toast.warning('업로드 중...');

            setTimeout(() => {
              toast.success('업로드 완료!');
            }, 2000);
          }}
          style={{
            padding: '12px 24px',
            backgroundColor: '#8B5A2B',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
          }}
        >
          업로드 시뮬레이션 (순차 메시지)
        </button>
      </div>
    </>
  );
};

export const Default: StoryObj = {
  render: () => <ToastExample />,
};
