

<template>
  <div id="app">
    <!-- Header -->
    <header class="app-header">
      <div class="header-container">
        <div class="logo-section">
          <div class="logo">
            <span class="logo-icon">📋</span>
            <h1 class="app-title">FLOW_TASK</h1>
          </div>
        </div>

        <div class="header-actions">
        </div>
      </div>
    </header>

    <!-- Tab Navigation -->
    <nav class="tab-navigation">
      <div class="tab-container">
        <RouterLink to="/" class="tab-item">
          <span class="tab-icon">📚</span>
          <span class="tab-text">확장자 관리</span>
        </RouterLink>
        <RouterLink to="/upload" class="tab-item">
          <span class="tab-icon">📋</span>
          <span class="tab-text">파일 업로드</span>
        </RouterLink>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="app-main">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-info">
            <span>© 2025 flow task. All rights reserved.</span>
          </div>
          <div class="footer-links">
            <a href="#" class="footer-link">도움말</a>
            <a href="#" class="footer-link">개인정보처리방침</a>
            <a href="#" class="footer-link">이용약관</a>
          </div>
        </div>
      </div>
    </footer>

    <!-- 전역 모달 -->

  </div>
</template>

<script setup>

</script>

<style scoped>
/* Global Layout */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #ffffff;
}

/* Header Styles */
.app-header {
  background: linear-gradient(
    135deg,
    rgba(55, 65, 81, 0.95) 0%,
    rgba(75, 85, 99, 0.95) 50%,
    rgba(31, 41, 55, 0.95) 100%
  );
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo-section {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.app-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.header-actions {
  display: flex;
  align-items: center;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.1);
  padding: 8px 16px;
  border-radius: 50px;
  backdrop-filter: blur(10px);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6b7280, #9ca3af, #4b5563);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.user-email {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.logout-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.05);
}

.login-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 10px 24px;
  border-radius: 50px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

/* Tab Navigation */
.tab-navigation {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 70px;
  z-index: 999;
  flex-shrink: 0;
}

.tab-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  padding: 0 24px;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 5px 24px;
  text-decoration: none;
  color: #666;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
  position: relative;
  min-width: 80px;
}

.tab-item:hover {
  color: #333;
  background-color: rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.tab-item.router-link-active {
  color: #374151;
  border-bottom-color: #6b7280;
  background: linear-gradient(to bottom, rgba(107, 114, 128, 0.08), transparent);
}

.tab-icon {
  font-size: 15px;
  filter: grayscale(0.3);
  transition: all 0.3s ease;
}

.tab-item.router-link-active .tab-icon {
  filter: grayscale(0);
  transform: scale(1.1);
}

.tab-text {
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Main Content */
.app-main {
  flex: 1;
  background-color: #ffffff;
  overflow-y: auto;
}

/* Footer */
.app-footer {
  background: linear-gradient(
    135deg,
    rgba(31, 41, 55, 0.95) 0%,
    rgba(55, 65, 81, 0.95) 50%,
    rgba(75, 85, 99, 0.95) 100%
  );
  color: rgba(255, 255, 255, 0.7);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}

.footer-info span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-link {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.footer-link:hover {
  color: rgba(255, 255, 255, 0.9);
}

/* Responsive Design */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
    height: 60px;
  }

  .app-title {
    font-size: 20px;
  }

  .logo-icon {
    font-size: 28px;
  }

  .user-details {
    display: none;
  }

  .user-info {
    padding: 6px 12px;
  }

  .tab-container {
    padding: 0 16px;
    overflow-x: auto;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .tab-container::-webkit-scrollbar {
    display: none;
  }

  .tab-item {
    padding: 12px 16px;
    min-width: 70px;
    flex-shrink: 0;
  }

  .tab-icon {
    font-size: 18px;
  }

  .tab-text {
    font-size: 11px;
  }

  .footer-content {
    flex-direction: column;
    height: auto;
    padding: 20px 0;
    gap: 12px;
    text-align: center;
  }

  .footer-links {
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
  }

  .app-main {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .header-container {
    padding: 0 12px;
  }

  .tab-container {
    padding: 0 12px;
  }

  .tab-item {
    padding: 10px 12px;
    min-width: 60px;
  }

  .footer-content {
    padding: 16px 0;
  }

  .footer-info span,
  .footer-link {
    font-size: 13px;
  }
}
</style>
