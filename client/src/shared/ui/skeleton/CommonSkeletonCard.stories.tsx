import { Meta, StoryObj } from '@storybook/react-webpack5';
import { CommonSkeletonCard } from './CommonSkeletonCard';

const meta = {
  title: 'Shared/CommonSkeletonCard',
  component: CommonSkeletonCard,
  parameters: {
    layout: 'fullscreen',
  },
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: { type: 'select' },
      options: ['moment', 'comment'],
      description: '스켈레톤 카드의 타입을 선택합니다.',
    },
  },
} satisfies Meta<typeof CommonSkeletonCard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Moment: Story = {
  args: {
    variant: 'moment',
  },
  parameters: {
    docs: {
      description: {
        story:
          'MyMoments 페이지에서 사용되는 스켈레톤 카드입니다. 타이틀, 컨텐츠, 이모지 버튼 영역을 포함합니다.',
      },
    },
  },
};

export const Comment: Story = {
  args: {
    variant: 'comment',
  },
  parameters: {
    docs: {
      description: {
        story:
          'MyComments 페이지에서 사용되는 스켈레톤 카드입니다. 원본 모멘트, 보낸 공감, 받은 스티커 영역을 포함합니다.',
      },
    },
  },
};

export const Multiple: Story = {
  render: () => (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '20px',
      }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
        <h3 style={{ color: 'white', margin: 0 }}>Moment 스켈레톤 (3개)</h3>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`moment-${index}`} variant="moment" />
        ))}
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
        <h3 style={{ color: 'white', margin: 0 }}>Comment 스켈레톤 (3개)</h3>
        {Array.from({ length: 3 }).map((_, index) => (
          <CommonSkeletonCard key={`comment-${index}`} variant="comment" />
        ))}
      </div>
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: '실제 페이지에서 사용되는 것처럼 여러 개의 스켈레톤 카드를 보여줍니다.',
      },
    },
  },
};

export const Comparison: Story = {
  render: () => (
    <div
      style={{
        display: 'flex',
        gap: '20px',
        flexWrap: 'wrap',
        justifyContent: 'center',
      }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
        <h4 style={{ color: 'white', margin: 0 }}>Moment</h4>
        <CommonSkeletonCard variant="moment" />
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
        <h4 style={{ color: 'white', margin: 0 }}>Comment</h4>
        <CommonSkeletonCard variant="comment" />
      </div>
    </div>
  ),
  parameters: {
    docs: {
      description: {
        story: '두 가지 variant를 나란히 비교해볼 수 있습니다.',
      },
    },
  },
};
