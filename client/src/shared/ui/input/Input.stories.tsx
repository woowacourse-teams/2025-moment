import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Input } from './Input';

const meta: Meta = {
  title: 'Example/Input',
  component: Input,
  argTypes: {
    type: {
      control: { type: 'radio' },
      options: ['text', 'password', 'email'],
    },
  },
  args: {
    placeholder: 'Enter your email',
    type: 'text',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};
