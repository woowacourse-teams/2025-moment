import { Meta, StoryObj } from '@storybook/react-webpack5';
import { SimpleCard } from './SimpleCard';

const meta: Meta = {
  title: 'Example/Text',
  component: SimpleCard,
  argTypes: {
    height: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'],
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
