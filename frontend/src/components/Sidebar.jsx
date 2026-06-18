import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Sidebar.css';

const Sidebar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/v1/auth/logout');
    } catch (err) {
      console.error('로그아웃 에러:', err);
    } finally {
      localStorage.removeItem('authToken');
      navigate('/login');
    }
  };

  return (
    <div className="sidebar-root">
      {/* 1. 데스크톱 사이드바 (lg 이상 활성화) */}
      <aside className="desktop-sidebar">


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
            >
              <span className="material-symbols-outlined menu-link-icon">
                settings
              </span>
              <span>Settings</span>
            </NavLink>
          </li>
          <li>
            <button 
              onClick={handleLogout}
              className="menu-link logout-btn"
              style={{
                background: 'none',
                border: 'none',
                width: '100%',
                textAlign: 'left',
                cursor: 'pointer',
                font: 'inherit'
              }}
            >
              <span className="material-symbols-outlined menu-link-icon">
                logout
              </span>
              <span>로그아웃</span>
            </button>
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

      {/* 3. 모바일 상단 헤더 & 햄버거 메뉴 (767px 이하 활성화) */}
      <header className="mobile-top-header">
        <div className="mobile-brand">
          <span className="material-symbols-outlined brand-logo">work</span>
          <span className="brand-name">HireSync</span>
        </div>
        <button 
          className={`hamburger-btn ${isOpen ? 'open' : ''}`} 
          onClick={() => setIsOpen(!isOpen)} 
          aria-label="메뉴 열기"
        >
          <span className="hamburger-line"></span>
          <span className="hamburger-line"></span>
          <span className="hamburger-line"></span>
        </button>
      </header>

      {/* 4. 모바일 햄버거 메뉴 드롭다운/슬라이드 오버레이 (767px 이하 활성화 및 isOpen 일때만 노출) */}
      <div 
        className={`mobile-menu-overlay ${isOpen ? 'open' : ''}`} 
        onClick={() => setIsOpen(false)}
      >
        <nav className="mobile-menu-drawer" onClick={(e) => e.stopPropagation()}>
          <div className="drawer-header" style={{ justifyContent: 'flex-end' }}>
            <button className="drawer-close-btn" onClick={() => setIsOpen(false)}>&times;</button>
          </div>
          <ul className="drawer-menu-list">
            <li>
              <NavLink 
                to="/admin" 
                className={({ isActive }) => isActive ? "drawer-link active" : "drawer-link"}
                onClick={() => setIsOpen(false)}
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
                className={({ isActive }) => isActive ? "drawer-link active" : "drawer-link"}
                onClick={() => setIsOpen(false)}
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
                className={({ isActive }) => isActive ? "drawer-link active" : "drawer-link"}
                onClick={() => setIsOpen(false)}
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
                className={({ isActive }) => isActive ? "drawer-link active" : "drawer-link"}
                onClick={() => setIsOpen(false)}
              >
                <span className="material-symbols-outlined menu-link-icon">
                  settings
                </span>
                <span>Settings</span>
              </NavLink>
            </li>
            <li>
              <button 
                onClick={(e) => {
                  handleLogout(e);
                  setIsOpen(false);
                }}
                className="drawer-link logout-btn"
                style={{
                  background: 'none',
                  border: 'none',
                  width: '100%',
                  textAlign: 'left',
                  cursor: 'pointer',
                  font: 'inherit'
                }}
              >
                <span className="material-symbols-outlined menu-link-icon">
                  logout
                </span>
                <span>로그아웃</span>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  );
};

export default Sidebar;
