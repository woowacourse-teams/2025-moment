import { Meta, StoryObj } from '@storybook/react-webpack5';
import { TitleContainer } from './TitleContainer';

const meta: Meta<typeof TitleContainer> = {
  title: 'Shared/TitleContainer',
  component: TitleContainer,
  args: {
    title: '오늘의 모멘트',
    subtitle: '하루에 한 번, 당신의 특별한 모멘트를 공유해보세요',
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};
