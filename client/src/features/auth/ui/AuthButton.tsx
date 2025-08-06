import { useLogoutMutation } from '@/features/auth/hooks/useLogoutMutation';
import { useProfileQuery } from '@/features/auth/hooks/useProfileQuery';
import { useOutsideClick } from '@/shared/hooks/useOutsideClick';
import { useToggle } from '@/shared/hooks/useToggle';
import { Button } from '@/shared/ui/button/Button';
import { useRef } from 'react';
import { useNavigate } from 'react-router';
import * as S from './AuthButton.styles';

interface AuthButtonProps {
  onClick?: () => void;
}

export const AuthButton = ({ onClick }: AuthButtonProps) => {
  const navigate = useNavigate();
  const { data: profile, isError } = useProfileQuery();
  const { mutate: logout } = useLogoutMutation();
  const { isOpen: isDropdownOpen, toggle: toggleDropdown } = useToggle(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLDivElement>(null);

  const closeDropdown = () => {
    if (isDropdownOpen) {
      toggleDropdown();
    }
  };

  useOutsideClick({
    ref: dropdownRef,
    callback: closeDropdown,
    isActive: isDropdownOpen,
    excludeRefs: [buttonRef],
  });

  const handleLoginClick = () => {
    onClick?.();
    navigate('/login');
  };

  const handleNicknameClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    toggleDropdown();
  };

  const handleLogoutClick = () => {
    logout();
    closeDropdown();
    onClick?.();
  };

  if (isError || !profile) {
    return <Button title="로그인" onClick={handleLoginClick} variant="primary" />;
  }

  return (
    <S.AuthButtonContainer ref={buttonRef} onClick={handleNicknameClick}>
      <S.AuthButtonText>{profile.nickname}</S.AuthButtonText>

      <S.DropdownContainer ref={dropdownRef} $isOpen={isDropdownOpen}>
        <S.DropdownItem onClick={handleLogoutClick}>로그아웃</S.DropdownItem>
      </S.DropdownContainer>
    </S.AuthButtonContainer>
  );
};
