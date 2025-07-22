import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Category } from './Category';

const meta: Meta = {
  title: 'Example/Category',
  component: Category,
  args: {
    text: '위로가 필요해요',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};
