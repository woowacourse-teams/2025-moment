import { Group } from '../types/group';
import * as S from './GroupCard.styles';
import { MoreVertical, Edit, Trash2, LogOut } from 'lucide-react';
import { useState, useRef } from 'react';
import { useOutsideClick } from '@/shared/hooks/useOutsideClick';

interface GroupCardProps {
  group: Group;
  onClick: () => void;
  onEdit?: (group: Group) => void;
  onDelete?: (groupId: number) => void;
  onLeave?: (groupId: number) => void;
}

export function GroupCard({ group, onClick, onEdit, onDelete, onLeave }: GroupCardProps) {
  const isOwner = group.isOwner;
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useOutsideClick({
    ref: menuRef,
    callback: () => setIsMenuOpen(false),
    isActive: isMenuOpen,
  });

  const handleMenuClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsMenuOpen(!isMenuOpen);
  };

  const handleAction = (e: React.MouseEvent, action: () => void) => {
    e.stopPropagation();
    action();
    setIsMenuOpen(false);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const showMenu = (isOwner && (onEdit || onDelete)) || (!isOwner && onLeave);

  return (
    <S.CardContainer onClick={onClick} role="button" tabIndex={0} aria-label={`${group.name} 그룹`}>
      <S.CardHeader>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
          <S.GroupName>{group.name}</S.GroupName>
          {isOwner && <S.OwnerBadge>방장</S.OwnerBadge>}
        </div>
        {showMenu && (
          <>
            <S.MenuButton onClick={handleMenuClick}>
              <MoreVertical size={16} />
            </S.MenuButton>
            {isMenuOpen && (
              <S.MenuDropdown ref={menuRef}>
                {isOwner ? (
                  <>
                    <S.MenuItem onClick={e => onEdit && handleAction(e, () => onEdit(group))}>
                      <Edit size={14} /> 수정하기
                    </S.MenuItem>
                    <S.MenuItem
                      className="danger"
                      onClick={e => onDelete && handleAction(e, () => onDelete(group.groupId))}
                    >
                      <Trash2 size={14} /> 삭제하기
                    </S.MenuItem>
                  </>
                ) : (
                  <S.MenuItem
                    className="danger"
                    onClick={e => onLeave && handleAction(e, () => onLeave(group.groupId))}
                  >
                    <LogOut size={14} /> 탈퇴하기
                  </S.MenuItem>
                )}
              </S.MenuDropdown>
            )}
          </>
        )}
      </S.CardHeader>

      {group.description && <S.Description>{group.description}</S.Description>}

      <S.CardFooter>
        <S.MemberCount>멤버 {group.memberCount}명</S.MemberCount>
        {group.createdAt && <S.CreatedDate>{formatDate(group.createdAt)}</S.CreatedDate>}
      </S.CardFooter>
    </S.CardContainer>
  );
}
