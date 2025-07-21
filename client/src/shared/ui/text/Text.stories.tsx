import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Text } from './Text';

const meta: Meta = {
  title: 'Example/Text',
  component: Text,
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
