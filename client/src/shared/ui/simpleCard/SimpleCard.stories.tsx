import { Meta, StoryObj } from '@storybook/react-webpack5';
import { SimpleCard } from './SimpleCard';

const meta: Meta<typeof SimpleCard> = {
  title: 'Shared/SimpleCard',
  component: SimpleCard,
  argTypes: {
    height: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'],
    },
    backgroundColor: {
      control: { type: 'select' },
      options: [undefined, 'gray-600_20', 'yellow-300_10', 'emerald-50', 'slate-800', 'blue-600'],
    },
  },
  args: {
    height: 'small',
    content: '텍스트 상자입니다.',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const WithCustomColor: Story = {
  args: {
    backgroundColor: 'emerald-50',
    content: '커스텀 배경색이 적용된 카드입니다.',
  },
};
