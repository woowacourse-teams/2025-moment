import { Meta, StoryObj } from '@storybook/react-webpack5';
import { TextArea } from './TextArea';

const meta: Meta = {
  title: 'Example/TextArea',
  component: TextArea,
  argTypes: {
    height: {
      control: { type: 'radio' },
      options: ['small', 'medium', 'large'],
    },
  },
  args: {
    placeholder: 'Enter your message',
    height: 'small',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};
