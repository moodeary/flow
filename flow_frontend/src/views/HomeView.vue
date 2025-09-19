<template>
  <div class="home-container">
    <div class="header">
      <h1 class="title">파일 확장자 차단 관리</h1>
      <p class="description">파일 확장자를 관리하고 테스트할 수 있습니다.</p>
    </div>

    <!-- 메인 컨텐츠 영역 -->
    <div class="main-content">
      <!-- 왼쪽: 고정/커스텀 확장자 섹션 (위아래 배치) -->
      <div class="left-column">
        <!-- 고정 확장자 섹션 -->
        <FixedExtensions ref="fixedExtensionsRef" />

        <!-- 커스텀 확장자 섹션 -->
        <CustomExtensions ref="customExtensionsRef" />
      </div>

      <!-- 오른쪽: 확장자 테스트 섹션 -->
      <div class="right-column">
        <ExtensionTest @extension-unblocked="handleExtensionUnblocked" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FixedExtensions from '@/components/extensions/FixedExtensions.vue'
import CustomExtensions from '@/components/extensions/CustomExtensions.vue'
import ExtensionTest from '@/components/extensions/ExtensionTest.vue'

const fixedExtensionsRef = ref(null)
const customExtensionsRef = ref(null)

/**
 * 확장자 차단 해제 후 목록 새로고침
 */
const handleExtensionUnblocked = (event) => {
  console.log('🔄 확장자 차단 해제됨:', event)

  // 고정 확장자가 해제된 경우 고정 확장자 목록 새로고침
  if (event.type === 'fixed' && fixedExtensionsRef.value?.loadFixedExtensions) {
    fixedExtensionsRef.value.loadFixedExtensions()
  }

  // 커스텀 확장자가 해제된 경우 커스텀 확장자 목록 새로고침
  if (event.type === 'custom' && customExtensionsRef.value?.loadCustomExtensions) {
    customExtensionsRef.value.loadCustomExtensions()
  }
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 12px;
}

.header {
  text-align: center;
  margin-bottom: 20px;
}

.title {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #374151, #6b7280, #9ca3af);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -1px;
  margin: 0 0 8px 0;
}

.description {
  font-size: 12px;
  color: var(--color-foreground-secondary);
  margin: 0;
}

.main-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.left-column {
  width: 60%;
  min-width: 0;
}

.right-column {
  width: 40%;
  min-width: 0;
}

@media (max-width: 768px) {
  .home-container {
    padding: 24px 16px;
  }

  .title {
    font-size: 24px;
  }

  .main-content {
    flex-direction: column;
    gap: 24px;
  }
}
</style>
