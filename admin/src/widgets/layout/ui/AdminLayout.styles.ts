import styled from "@emotion/styled";
import { NavLink } from "react-router-dom";

export const LayoutWrapper = styled.div`
  display: flex;
  min-height: 100vh;
`;

export const Sidebar = styled.aside`
  width: 240px;
  min-height: 100vh;
  background-color: #1e293b;
  color: #f1f5f9;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
`;

export const Logo = styled.div`
  padding: 1.5rem;
  font-size: 1.125rem;
  font-weight: 700;
  color: #fff;
  border-bottom: 1px solid #334155;
`;

export const Nav = styled.nav`
  flex: 1;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
`;

export const SidebarNavLink = styled(NavLink)`
  display: flex;
  align-items: center;
  padding: 0.625rem 1.5rem;
  color: #94a3b8;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;

  &:hover {
    background-color: #334155;
    color: #f1f5f9;
  }

  &.active {
    background-color: #3b82f6;
    color: #fff;
  }
`;

export const SidebarFooter = styled.div`
  padding: 1rem 1.5rem;
  border-top: 1px solid #334155;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
`;

export const UserInfoBlock = styled.div`
  font-size: 0.75rem;
  color: #94a3b8;
  line-height: 1.4;
`;

export const UserEmail = styled.div`
  color: #e2e8f0;
  font-weight: 500;
  word-break: break-all;
`;

export const UserRole = styled.div`
  text-transform: uppercase;
  font-size: 0.6875rem;
  letter-spacing: 0.05em;
  margin-top: 0.125rem;
`;

export const LogoutButton = styled.button`
  padding: 0.5rem;
  border: 1px solid #475569;
  border-radius: 6px;
  background: transparent;
  color: #94a3b8;
  font-size: 0.8125rem;
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;

  &:hover {
    background-color: #ef4444;
    border-color: #ef4444;
    color: #fff;
  }
`;

export const Content = styled.main`
  flex: 1;
  margin-left: 240px;
  background-color: #f9fafb;
  min-height: 100vh;
  padding: 2rem;
`;
