import { useState } from 'react';
import { Modal } from '@/shared/design-system/modal/Modal';
import { useGroupMembersQuery } from '../api/useGroupMembersQuery';
import { usePendingMembersQuery } from '../api/usePendingMembersQuery';
import { useApproveMemberMutation } from '../api/useApproveMemberMutation';
import { useRejectMemberMutation } from '../api/useRejectMemberMutation';
import { useKickMemberMutation } from '../api/useKickMemberMutation';
import { useTransferOwnershipMutation } from '../api/useTransferOwnershipMutation';
import styled from '@emotion/styled';
import { User, UserPlus, UserMinus, ShieldCheck, XCircle, CheckCircle } from 'lucide-react';
import { theme } from '@/shared/styles/theme';

interface GroupMemberManagementModalProps {
  groupId: number | string;
  isOpen: boolean;
  onClose: () => void;
}

type Tab = 'members' | 'pending';

export const GroupMemberManagementModal = ({
  groupId,
  isOpen,
  onClose,
}: GroupMemberManagementModalProps) => {
  const [activeTab, setActiveTab] = useState<Tab>('members');

  const { data: membersData, isLoading: isMembersLoading } = useGroupMembersQuery(groupId);
  const { data: pendingData, isLoading: isPendingLoading } = usePendingMembersQuery(groupId);

  const approveMutation = useApproveMemberMutation(groupId);
  const rejectMutation = useRejectMemberMutation(groupId);
  const kickMutation = useKickMemberMutation(groupId);
  const transferMutation = useTransferOwnershipMutation(groupId);

  const members = membersData?.data || [];
  const pending = pendingData?.data || [];

  const handleApprove = (memberId: number) => {
    if (window.confirm('이 회원을 승인하시겠습니까?')) {
      approveMutation.mutate(memberId);
    }
  };

  const handleReject = (memberId: number) => {
    if (window.confirm('이 회원의 가입 요청을 거절하시겠습니까?')) {
      rejectMutation.mutate(memberId);
    }
  };

  const handleKick = (memberId: number) => {
    if (window.confirm('정말로 이 회원을 강퇴하시겠습니까?')) {
      kickMutation.mutate(memberId);
    }
  };

  const handleTransfer = (memberId: number) => {
    if (
      window.confirm(
        '정말로 이 회원에게 방장 소유권을 이전하시겠습니까?\n이전 후 귀하는 더 이상 방장이 아닙니다.',
      )
    ) {
      transferMutation.mutate(memberId);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="medium">
      <Modal.Header title="그룹 멤버 관리" />
      <Modal.Content>
        <TabContainer>
          <TabButton active={activeTab === 'members'} onClick={() => setActiveTab('members')}>
            멤버 목록 ({members.length})
          </TabButton>
          <TabButton active={activeTab === 'pending'} onClick={() => setActiveTab('pending')}>
            가입 대기 ({pending.length})
          </TabButton>
        </TabContainer>

        <ListContainer>
          {activeTab === 'members' ? (
            isMembersLoading ? (
              <EmptyText>로딩 중...</EmptyText>
            ) : members.length === 0 ? (
              <EmptyText>멤버가 없습니다.</EmptyText>
            ) : (
              members.map(member => {
                const memberId = member.memberId;
                return (
                  <MemberItem key={memberId}>
                    <MemberInfo>
                      <User size={24} />
                      <div>
                        <Nickname>{member.nickname}</Nickname>
                        <RoleBadge isOwner={member.role === 'OWNER'}>
                          {member.role === 'OWNER' ? '방장' : '멤버'}
                        </RoleBadge>
                      </div>
                    </MemberInfo>
                    {member.role !== 'OWNER' && (
                      <ActionButtons>
                        <ActionButton
                          title="소유권 이전"
                          variant="secondary"
                          onClick={() => handleTransfer(memberId)}
                        >
                          <ShieldCheck size={16} />
                        </ActionButton>
                        <ActionButton
                          title="강퇴"
                          variant="danger"
                          onClick={() => handleKick(memberId)}
                        >
                          <UserMinus size={16} />
                        </ActionButton>
                      </ActionButtons>
                    )}
                  </MemberItem>
                );
              })
            )
          ) : isPendingLoading ? (
            <EmptyText>로딩 중...</EmptyText>
          ) : pending.length === 0 ? (
            <EmptyText>대기 중인 회원이 없습니다.</EmptyText>
          ) : (
            pending.map(applicant => {
              const applicantId = applicant.memberId;
              return (
                <MemberItem key={applicantId}>
                  <MemberInfo>
                    <UserPlus size={24} />
                    <div>
                      <Nickname>{applicant.nickname}</Nickname>
                      <RequestTime>
                        신청일: {new Date(applicant.requestedAt).toLocaleDateString()}
                      </RequestTime>
                    </div>
                  </MemberInfo>
                  <ActionButtons>
                    <ActionButton
                      title="승인"
                      variant="primary"
                      onClick={() => handleApprove(applicantId)}
                    >
                      <CheckCircle size={16} />
                    </ActionButton>
                    <ActionButton
                      title="거절"
                      variant="danger"
                      onClick={() => handleReject(applicantId)}
                    >
                      <XCircle size={16} />
                    </ActionButton>
                  </ActionButtons>
                </MemberItem>
              );
            })
          )}
        </ListContainer>
      </Modal.Content>
    </Modal>
  );
};

const TabContainer = styled.div`
  display: flex;
  border-bottom: 1px solid ${theme.colors['gray-200']};
  margin-bottom: 16px;
`;

const TabButton = styled.button<{ active: boolean }>`
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  border-bottom: 2px solid ${props => (props.active ? theme.colors['yellow-500'] : 'transparent')};
  color: ${props => (props.active ? theme.colors['yellow-500'] : theme.colors['gray-600'])};
  font-weight: ${props => (props.active ? 'bold' : 'normal')};
  cursor: pointer;

  &:hover {
    background-color: ${theme.colors['slate-900_60']};
    color: ${theme.colors.white};
  }
`;

const ListContainer = styled.div`
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const MemberItem = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background-color: ${theme.colors['slate-800']};
  color: ${theme.colors.white};
`;

const MemberInfo = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
`;

const Nickname = styled.p`
  font-weight: 500;
  margin: 0;
`;

const RequestTime = styled.span`
  font-size: 12px;
  color: ${theme.colors.white_70};
`;

const RoleBadge = styled.span<{ isOwner: boolean }>`
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: ${props =>
    props.isOwner ? theme.colors['yellow-500'] : theme.colors['gray-600']};
  color: ${props => (props.isOwner ? theme.colors['yellow-600'] : theme.colors.white)};
`;

const ActionButtons = styled.div`
  display: flex;
  gap: 8px;
`;

const ActionButton = styled.button<{ variant: 'primary' | 'secondary' | 'danger' }>`
  background: none;
  border: 1px solid;
  border-color: ${props => {
    if (props.variant === 'primary') return theme.colors['yellow-500'];
    if (props.variant === 'danger') return theme.colors['red-500'];
    return theme.colors['gray-400'];
  }};
  color: ${props => {
    if (props.variant === 'primary') return theme.colors['yellow-500'];
    if (props.variant === 'danger') return theme.colors['red-500'];
    return theme.colors.white;
  }};
  padding: 6px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background-color: ${props => {
      if (props.variant === 'primary') return theme.colors['yellow-300_10'];
      if (props.variant === 'danger') return 'rgba(239, 68, 68, 0.1)';
      return theme.colors['gray-200_10'];
    }};
  }
`;

const EmptyText = styled.p`
  text-align: center;
  padding: 32px;
  color: ${theme.colors['gray-400']};
`;
