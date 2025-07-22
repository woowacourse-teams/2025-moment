import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Button } from './Button';

const meta: Meta = {
  title: 'Example/Button',
  component: Button,
  argTypes: {
    variant: {
      control: { type: 'radio' },
      options: ['primary', 'secondary'],
    },
  },
  args: {
    variant: 'primary',
    title: 'Click me',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};
