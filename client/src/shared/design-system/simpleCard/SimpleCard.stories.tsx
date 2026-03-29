import { Meta, StoryObj } from '@storybook/react-webpack5';
import { SimpleCard } from './SimpleCard';
import { SimpleCardHeight } from './SimpleCard.styles';

const meta: Meta<typeof SimpleCard> = {
  title: 'Design System/SimpleCard',
  component: SimpleCard,
  argTypes: {
    height: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'] satisfies SimpleCardHeight[],
    },
    backgroundColor: {
      control: { type: 'select' },
      options: [undefined, 'gray-600_20', 'yellow-300_10', 'emerald-50', 'slate-800', 'blue-600'],
    },
  },
  args: {
    height: 'small',
    children: '텍스트 상자입니다.',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const AllHeights: Story = {
  render: () => (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 24, background: '#1E293B' }}>
      {(['small', 'medium', 'large'] as SimpleCardHeight[]).map((height) => (
        <SimpleCard key={height} height={height}>
          height: {height}
        </SimpleCard>
      ))}
    </div>
  ),
};

export const WithCustomColor: Story = {
  args: {
    backgroundColor: 'emerald-50',
    children: '커스텀 배경색이 적용된 카드입니다.',
  },
};
