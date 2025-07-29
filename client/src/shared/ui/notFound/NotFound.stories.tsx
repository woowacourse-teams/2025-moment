import { Meta, StoryObj } from '@storybook/react-webpack5';
import { AlertCircle, Eye, Search } from 'lucide-react';
import { NotFound } from './NotFound';

const meta: Meta<typeof NotFound> = {
  title: 'Shared/NotFound',
  component: NotFound,
  argTypes: {
    size: {
      control: { type: 'radio' },
      options: ['small', 'large'],
    },
    withCard: {
      control: { type: 'boolean' },
    },
    iconSize: {
      control: { type: 'number' },
    },
  },
  args: {
    title: '페이지를 찾을 수 없습니다',
    subtitle: '요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다',
    icon: Eye,
    iconSize: 24,
    size: 'large',
    withCard: false,
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Small: Story = {
  args: {
    size: 'small',
    title: '검색 결과 없음',
    subtitle: '검색 조건을 변경해보세요',
  },
};

export const WithCard: Story = {
  args: {
    withCard: true,
    title: '데이터가 없습니다',
    subtitle: '아직 등록된 데이터가 없습니다',
  },
};

export const WithSearchIcon: Story = {
  args: {
    icon: Search,
    title: '검색 결과가 없습니다',
    subtitle: '다른 키워드로 검색해보세요',
  },
};

export const WithAlertIcon: Story = {
  args: {
    icon: AlertCircle,
    title: '오류가 발생했습니다',
    subtitle: '잠시 후 다시 시도해주세요',
    iconSize: 32,
  },
};

export const SmallWithCard: Story = {
  args: {
    size: 'small',
    withCard: true,
    title: '빈 상태',
    subtitle: '데이터가 없습니다',
  },
};
