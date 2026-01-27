import styled from '@emotion/styled';
import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@shared/auth/useAuth';
import { ConfirmModal } from '@shared/ui';
import { Pagination } from '@features/user/ui/Pagination';
import { GroupDetailCard } from '@features/group/ui/GroupDetailCard';
import { GroupEditModal } from '@features/group/ui/GroupEditModal';
import { GroupDeleteModal } from '@features/group/ui/GroupDeleteModal';
import { useGroupDetail } from '@features/group/hooks/useGroupDetail';
import { MemberTable } from '@features/member/ui/MemberTable';
import { PendingMemberTable } from '@features/member/ui/PendingMemberTable';
import { TransferOwnershipModal } from '@features/member/ui/TransferOwnershipModal';
import { useGroupMembers } from '@features/member/hooks/useGroupMembers';
import { MomentTable } from '@features/moment/ui/MomentTable';
import { useGroupMomentsQuery } from '@features/moment/api/useGroupMomentsQuery';
import { useDeleteMomentMutation } from '@features/moment/api/useDeleteMomentMutation';
import { CommentTable } from '@features/comment/ui/CommentTable';
import { useGroupCommentsQuery } from '@features/comment/api/useGroupCommentsQuery';
import { useDeleteCommentMutation } from '@features/comment/api/useDeleteCommentMutation';

type TabType = 'members' | 'pending' | 'moments';

const Container = styled.div`
  padding: 2rem;
  max-width: 1000px;
`;

const BackLink = styled.button`
  padding: 0.5rem 0;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;

  &:hover {
    color: #3b82f6;
  }
`;

const TabContainer = styled.div`
  margin-top: 2rem;
`;

const TabList = styled.div`
  display: flex;
  gap: 0;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 1.5rem;
`;

const Tab = styled.button<{ $active: boolean }>`
  padding: 0.75rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  background: none;
  border: none;
  border-bottom: 2px solid ${({ $active }) => ($active ? '#3b82f6' : 'transparent')};
  color: ${({ $active }) => ($active ? '#3b82f6' : '#6b7280')};
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;

  &:hover {
    color: ${({ $active }) => ($active ? '#3b82f6' : '#1f2937')};
  }
`;

const LoadingState = styled.div`
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #9ca3af;
`;

const ErrorState = styled.div`
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #ef4444;
`;

export default function GroupDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user: authUser } = useAuth();
  const isAdmin = authUser?.role === 'ADMIN';

  const [activeTab, setActiveTab] = useState<TabType>('members');
  const [momentPage, setMomentPage] = useState(0);
  const [commentPage, setCommentPage] = useState(0);
  const [selectedMomentId, setSelectedMomentId] = useState<string | null>(null);

  // Kick / Transfer confirmation state
  const [kickTarget, setKickTarget] = useState<number | null>(null);
  const [transferTarget, setTransferTarget] = useState<number | null>(null);

  const {
    group,
    isLoading,
    isError,
    isDeleteModalOpen,
    openDeleteModal,
    closeDeleteModal,
    deleteReason,
    setDeleteReason,
    handleDelete,
    isDeleting,
    handleRestore,
    isRestoring,
    isEditModalOpen,
    setEditModalOpen,
    handleUpdate,
    isUpdating,
  } = useGroupDetail(id!);

  const {
    members,
    membersTotalPages,
    memberPage,
    setMemberPage,
    isMembersLoading,
    pendingMembers,
    pendingTotalPages,
    pendingPage,
    setPendingPage,
    isPendingLoading,
    approveMember,
    rejectMember,
    kickMember,
    transferOwnership,
    isKicking,
    isTransferring,
  } = useGroupMembers(id!);

  const momentsQuery = useGroupMomentsQuery({
    groupId: id!,
    page: momentPage,
    size: 20,
  });

  const commentsQuery = useGroupCommentsQuery({
    groupId: id!,
    momentId: selectedMomentId ?? '',
    page: commentPage,
    size: 20,
  });

  const deleteMomentMutation = useDeleteMomentMutation(id!);
  const deleteCommentMutation = useDeleteCommentMutation(id!, selectedMomentId ?? '');

  if (isLoading) {
    return (
      <Container>
        <LoadingState>Loading...</LoadingState>
      </Container>
    );
  }

  if (isError || !group) {
    return (
      <Container>
        <ErrorState>Failed to load group details.</ErrorState>
      </Container>
    );
  }

  const handleMomentClick = (momentId: number) => {
    setSelectedMomentId(String(momentId));
    setCommentPage(0);
  };

  const handleBackToMoments = () => {
    setSelectedMomentId(null);
    setCommentPage(0);
  };

  const handleKickConfirm = async () => {
    if (kickTarget === null) return;
    await kickMember(kickTarget);
    setKickTarget(null);
  };

  const handleTransferConfirm = async () => {
    if (transferTarget === null) return;
    await transferOwnership(transferTarget);
    setTransferTarget(null);
  };

  const transferMember = members.find((m) => m.id === transferTarget);

  return (
    <Container>
      <BackLink onClick={() => navigate('/groups')}>&larr; Back to Groups</BackLink>

      <GroupDetailCard
        group={group}
        isAdmin={isAdmin}
        onEdit={() => setEditModalOpen(true)}
        onDelete={openDeleteModal}
        onRestore={handleRestore}
        isRestoring={isRestoring}
      />

      <TabContainer>
        <TabList>
          <Tab $active={activeTab === 'members'} onClick={() => setActiveTab('members')}>
            Members
          </Tab>
          <Tab $active={activeTab === 'pending'} onClick={() => setActiveTab('pending')}>
            Pending
          </Tab>
          <Tab
            $active={activeTab === 'moments'}
            onClick={() => {
              setActiveTab('moments');
              setSelectedMomentId(null);
            }}
          >
            Moments
          </Tab>
        </TabList>

        {activeTab === 'members' && (
          <>
            <MemberTable
              members={members}
              isLoading={isMembersLoading}
              isAdmin={isAdmin}
              onKick={(memberId) => setKickTarget(memberId)}
              onTransfer={(memberId) => setTransferTarget(memberId)}
            />
            <Pagination
              currentPage={memberPage}
              totalPages={membersTotalPages}
              onPageChange={setMemberPage}
            />
          </>
        )}

        {activeTab === 'pending' && (
          <>
            <PendingMemberTable
              pendingMembers={pendingMembers}
              isLoading={isPendingLoading}
              isAdmin={isAdmin}
              onApprove={approveMember}
              onReject={rejectMember}
            />
            <Pagination
              currentPage={pendingPage}
              totalPages={pendingTotalPages}
              onPageChange={setPendingPage}
            />
          </>
        )}

        {activeTab === 'moments' && !selectedMomentId && (
          <>
            <MomentTable
              moments={momentsQuery.data?.content ?? []}
              isLoading={momentsQuery.isLoading}
              isAdmin={isAdmin}
              onMomentClick={handleMomentClick}
              onDelete={(momentId) => deleteMomentMutation.mutate(momentId)}
            />
            <Pagination
              currentPage={momentPage}
              totalPages={momentsQuery.data?.totalPages ?? 0}
              onPageChange={setMomentPage}
            />
          </>
        )}

        {activeTab === 'moments' && selectedMomentId && (
          <>
            <CommentTable
              comments={commentsQuery.data?.content ?? []}
              isLoading={commentsQuery.isLoading}
              isAdmin={isAdmin}
              onDelete={(commentId) => deleteCommentMutation.mutate(commentId)}
              onBack={handleBackToMoments}
              momentId={selectedMomentId}
            />
            <Pagination
              currentPage={commentPage}
              totalPages={commentsQuery.data?.totalPages ?? 0}
              onPageChange={setCommentPage}
            />
          </>
        )}
      </TabContainer>

      {/* Modals */}
      <GroupEditModal
        isOpen={isEditModalOpen}
        onClose={() => setEditModalOpen(false)}
        currentName={group.name}
        currentDescription={group.description}
        onSubmit={handleUpdate}
        isLoading={isUpdating}
      />

      <GroupDeleteModal
        isOpen={isDeleteModalOpen}
        onClose={closeDeleteModal}
        reason={deleteReason}
        onReasonChange={setDeleteReason}
        onConfirm={handleDelete}
        isLoading={isDeleting}
      />

      <ConfirmModal
        isOpen={kickTarget !== null}
        onClose={() => setKickTarget(null)}
        onConfirm={handleKickConfirm}
        title="Kick Member"
        message="Are you sure you want to kick this member from the group?"
        confirmLabel="Kick"
        isDestructive
        isLoading={isKicking}
      />

      <TransferOwnershipModal
        isOpen={transferTarget !== null}
        onClose={() => setTransferTarget(null)}
        onConfirm={handleTransferConfirm}
        memberNickname={transferMember?.nickname ?? ''}
        isLoading={isTransferring}
      />
    </Container>
  );
}
