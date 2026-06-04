import React from 'react';
import { NavLink } from 'react-router-dom';
import './Sidebar.css';

const Sidebar = () => {
  // Stitch 스펙 아바타 이미지 주소
  const avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCYb0d6vKegftTck0XCuVKXgz-Ki8XxbChnwdy2rVDPGw5fBRTwje9E7tKCE7qN4ma3Qnar0ngdgmlb7U_E8gV5depsG_MdW4tXGlOvpy7rkC4Z4DQ5yG-rrBVXVwKNuuCskINGHFIP0qLLGz3bXVvegjH5uxr-ydOlQmuYveVeTIwpL1JcK4fUnck_HzeUyYOKWQxhqtpFm3g9_ybw_P-TssxKmmrTCGR51F0fj24cqCRdqwNyx_Ol20bbK2bBxeO1Kn_JvflkU0A6";

  return (
    <div className="sidebar-root">
      {/* 1. 데스크톱 사이드바 (lg 이상 활성화) */}
      <aside className="desktop-sidebar">
        {/* 프로필 헤더 */}
        <div className="sidebar-profile">
          <div className="profile-avatar-container">
            <img 
              src={avatarUrl} 
              alt="System Administrator Avatar" 
              className="profile-avatar" 
            />
          </div>
          <div className="profile-info">
            <span className="profile-name">Admin User</span>
            <span className="profile-role">System Administrator</span>
            <span className="profile-version">v1.0.4</span>
          </div>
        </div>

        {/* 메뉴 리스트 */}
        <ul className="sidebar-menu-list">
          <li>
            <NavLink 
              to="/admin" 
              className={({ isActive }) => isActive ? "menu-link active" : "menu-link"}
              end
            >
              <span className="material-symbols-outlined menu-link-icon" style={{ fontVariationSettings: "'FILL' 1" }}>
                dashboard
              </span>
              <span>Dashboard</span>
            </NavLink>
          </li>
          <li>
            <NavLink 
              to="/" 
              className={({ isActive }) => isActive ? "menu-link active" : "menu-link"}
              end
            >
              <span className="material-symbols-outlined menu-link-icon">
                work
              </span>
              <span>Job Listings</span>
            </NavLink>
          </li>
          <li>
            <NavLink 
              to="/users-management" 
              className={({ isActive }) => isActive ? "menu-link active" : "menu-link"}
            >
              <span className="material-symbols-outlined menu-link-icon">
                group
              </span>
              <span>User Management</span>
            </NavLink>
          </li>
          <li>
            <NavLink 
              to="/settings" 
              className={({ isActive }) => isActive ? "menu-link active" : "menu-link"}
              onClick={(e) => {
                e.preventDefault();
                alert('시스템 설정 기능은 후속 스프린트에서 구현 예정입니다.');
              }}
            >
              <span className="material-symbols-outlined menu-link-icon">
                settings
              </span>
              <span>System Settings</span>
            </NavLink>
          </li>
        </ul>
      </aside>

      {/* 2. 모바일/태블릿 하단 탭바 (lg 미만 활성화) */}
      <nav className="mobile-bottom-nav">
        <NavLink 
          to="/" 
          className={({ isActive }) => isActive ? "mobile-nav-item active" : "mobile-nav-item"}
          end
        >
          <span className="material-symbols-outlined mobile-nav-icon">
            work
          </span>
          <span className="mobile-nav-label">Jobs</span>
        </NavLink>
        
        <NavLink 
          to="/admin" 
          className={({ isActive }) => isActive ? "mobile-nav-item active-pill" : "mobile-nav-item"}
          end
        >
          <span className="material-symbols-outlined mobile-nav-icon" style={{ fontVariationSettings: "'FILL' 1" }}>
            dashboard
          </span>
          <span className="mobile-nav-label">Admin</span>
        </NavLink>

        <NavLink 
          to="/users-management" 
          className={({ isActive }) => isActive ? "mobile-nav-item active" : "mobile-nav-item"}
        >
          <span className="material-symbols-outlined mobile-nav-icon">
            group
          </span>
          <span className="mobile-nav-label">Users</span>
        </NavLink>
      </nav>
    </div>
  );
};

export default Sidebar;
