import { Meta, StoryObj } from '@storybook/react-webpack5';
import { Skeleton, SkeletonText } from './Skeleton';

const meta = {
    title: 'Design System/Skeleton',
    component: Skeleton,
    parameters: {
        layout: 'centered',
    },
    tags: ['autodocs'],
    argTypes: {
        width: {
            control: { type: 'text' },
            description: '스켈레톤의 너비',
        },
        height: {
            control: { type: 'text' },
            description: '스켈레톤의 높이',
        },
        borderRadius: {
            control: { type: 'text' },
            description: '스켈레톤의 모서리 둥글기',
        },
    },
} satisfies Meta<typeof Skeleton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
    args: {
        width: '200px',
        height: '20px',
        borderRadius: '4px',
    },
};

export const Circle: Story = {
    args: {
        width: '40px',
        height: '40px',
        borderRadius: '50%',
    },
    parameters: {
        docs: {
            description: {
                story: '프로필 이미지나 아바타에 사용되는 원형 스켈레톤',
            },
        },
    },
};

export const Rectangle: Story = {
    args: {
        width: '300px',
        height: '200px',
        borderRadius: '8px',
    },
    parameters: {
        docs: {
            description: {
                story: '이미지나 카드에 사용되는 사각형 스켈레톤',
            },
        },
    },
};

export const Text: Story = {
    render: () => (
        <div style={{ width: '300px' }}>
            <SkeletonText lines={3} lineHeight="16px" gap="8px" />
        </div>
    ),
    parameters: {
        docs: {
            description: {
                story: '여러 줄의 텍스트를 표현하는 스켈레톤. 마지막 줄은 자동으로 75% 너비로 설정됩니다.',
            },
        },
    },
};

export const CardExample: Story = {
    render: () => (
        <div
            style={{
                width: '350px',
                padding: '20px',
                backgroundColor: '#1a1a1a',
                borderRadius: '8px',
                display: 'flex',
                flexDirection: 'column',
                gap: '16px',
            }}
        >
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <Skeleton width="40px" height="40px" borderRadius="50%" />
                <div style={{ flex: 1 }}>
                    <Skeleton width="120px" height="16px" />
                    <div style={{ marginTop: '8px' }}>
                        <Skeleton width="80px" height="12px" />
                    </div>
                </div>
            </div>
            <SkeletonText lines={3} lineHeight="14px" gap="8px" />
            <Skeleton width="100%" height="200px" borderRadius="8px" />
        </div>
    ),
    parameters: {
        docs: {
            description: {
                story: '기본 Skeleton 컴포넌트를 조합하여 카드 레이아웃을 만드는 예시',
            },
        },
    },
};

export const Variants: Story = {
    render: () => (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
                <h4 style={{ color: 'white', marginBottom: '10px' }}>Circles</h4>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <Skeleton width="20px" height="20px" borderRadius="50%" />
                    <Skeleton width="30px" height="30px" borderRadius="50%" />
                    <Skeleton width="40px" height="40px" borderRadius="50%" />
                    <Skeleton width="50px" height="50px" borderRadius="50%" />
                </div>
            </div>
            <div>
                <h4 style={{ color: 'white', marginBottom: '10px' }}>Lines</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    <Skeleton width="100%" height="12px" />
                    <Skeleton width="100%" height="12px" />
                    <Skeleton width="75%" height="12px" />
                </div>
            </div>
            <div>
                <h4 style={{ color: 'white', marginBottom: '10px' }}>Rectangles</h4>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <Skeleton width="100px" height="100px" borderRadius="4px" />
                    <Skeleton width="100px" height="100px" borderRadius="8px" />
                    <Skeleton width="100px" height="100px" borderRadius="12px" />
                </div>
            </div>
        </div>
    ),
    parameters: {
        docs: {
            description: {
                story: '다양한 크기와 모양의 스켈레톤 변형 예시',
            },
        },
    },
};
