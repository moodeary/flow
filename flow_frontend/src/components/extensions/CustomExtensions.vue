<template>
  <div class="section">
    <div class="section-header">
      <div class="section-info">
        <h2 class="section-title">커스텀 확장자</h2>
        <p class="section-desc">최대 200개까지 추가할 수 있습니다. (현재: {{ customExtensions.length }}/200)</p>
      </div>
      <button
        v-if="customExtensions.length > 0"
        class="clear-all-btn"
        @click="clearAllCustomExtensions"
        :disabled="loading"
        title="전체 삭제"
      >
        전체 삭제
      </button>
    </div>

    <div class="custom-input-group">
      <InputField
        v-model="newCustomExtension"
        placeholder="확장자 입력 (예: sh, php)"
        :maxlength="20"
        :show-counter="true"
        :error-message="customInputError"
        @enter="addCustomExtension"
      />
      <button
        class="add-btn"
        :disabled="!newCustomExtension.trim() || customExtensions.length >= 200 || loading"
        @click="addCustomExtension"
      >
        추가
      </button>
    </div>

    <div v-if="loading" class="loading">커스텀 확장자를 불러오는 중...</div>
    <div v-else-if="customExtensions.length === 0" class="empty">추가된 커스텀 확장자가 없습니다.</div>
    <div v-else class="custom-extensions">
      <div
        v-for="extension in customExtensions"
        :key="extension.id"
        class="custom-extension-tag"
      >
        <span class="tag-text">{{ extension.extension }}</span>
        <button
          class="remove-btn"
          @click="removeCustomExtension(extension.id)"
          :disabled="loading"
        >
          ×
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import InputField from '@/components/common/InputField.vue'
import { useExtensionStore } from '@/stores/extension'

const newCustomExtension = ref('')
const customInputError = ref('')

// Pinia 스토어 사용
const extensionStore = useExtensionStore()
const { customExtensions, isLoadingCustom: loading } = storeToRefs(extensionStore)

/**
 * 커스텀 확장자 목록을 서버에서 조회하는 함수
 */
const fetchCustomExtensions = async () => {
  const result = await extensionStore.loadCustomExtensions()
  if (!result.success) {
    alert('커스텀 확장자 목록을 불러오는데 실패했습니다.')
  }
}

/**
 * 커스텀 확장자 입력값의 유효성을 검증하는 함수
 * - 빈 값, 길이 제한, 한글 포함, 올바른 형식, 중복 확인 등의 검증 수행
 * - 에러 메시지 반환 또는 빈 문자열(유효함) 반환
 */
const validateCustomExtension = (extension) => {
  if (!extension.trim()) {
    return '확장자를 입력해주세요.'
  }

  if (extension.length > 20) {
    return '확장자는 최대 20자까지 입력 가능합니다.'
  }

  // 한글이 포함되어 있는지 확인 (전체 입력값에 대해)
  const koreanRegex = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/
  if (koreanRegex.test(extension)) {
    return '확장자에는 한글을 사용할 수 없습니다. 영문자와 숫자만 입력해주세요.'
  }

  // . 이 포함된 경우 잘못된 형식으로 판단
  if (extension.includes('.')) {
    return '올바른 확장자 형식을 입력해주세요. 점(.)을 제외하고 입력해주세요. (예: sh, php)'
  }

  // 영문자, 숫자만 허용하는 정규식
  const validExtensionRegex = /^[a-zA-Z0-9]+$/
  if (!validExtensionRegex.test(extension)) {
    return '확장자는 영문자와 숫자만 사용할 수 있습니다.'
  }

  // 커스텀 확장자 중복 확인
  if (customExtensions.value.some(ext => ext.extension === extension.trim())) {
    return '커스텀 확장자에 이미 있는 확장자입니다.'
  }

  return '' // 유효함
}

/**
 * 새로운 커스텀 확장자를 추가하는 함수
 */
const addCustomExtension = async () => {
  const extension = newCustomExtension.value.trim()
  const error = validateCustomExtension(extension)

  if (error) {
    customInputError.value = error
    return
  }

  // 최대 개수 제한 확인
  if (customExtensions.value.length >= 200) {
    customInputError.value = '최대 200개까지만 추가할 수 있습니다.'
    return
  }

  const result = await extensionStore.addCustomExtension(extension)
  if (result.success) {
    newCustomExtension.value = '' // 입력 폼 초기화
    customInputError.value = ''
  } else {
    customInputError.value = result.error || '추가에 실패했습니다.'
  }
}

/**
 * 커스텀 확장자를 삭제하는 함수
 */
const removeCustomExtension = async (id) => {
  const extension = customExtensions.value.find(ext => ext.id === id)
  if (!extension) return

  // 삭제 확인
  if (!confirm(`"${extension.extension}" 확장자를 삭제하시겠습니까?`)) {
    return
  }

  const result = await extensionStore.deleteCustomExtension(id)
  if (!result.success) {
    alert('커스텀 확장자 삭제에 실패했습니다.')
  }
}

// 컴포넌트 마운트 시 초기 데이터 로드
onMounted(() => {
  fetchCustomExtensions() // 커스텀 확장자 목록 조회
})

// 부모 컴포넌트에서 호출할 수 있는 메서드 expose
defineExpose({
  loadCustomExtensions: fetchCustomExtensions
})

/**
 * 모든 커스텀 확장자를 삭제하는 함수
 */
const clearAllCustomExtensions = async () => {
  if (!confirm(`모든 커스텀 확장자(${customExtensions.value.length}개)를 삭제하시겠습니까?`)) {
    return
  }

  const result = await extensionStore.deleteAllCustomExtensions()
  if (!result.success) {
    alert('커스텀 확장자 전체 삭제에 실패했습니다.')
  }
}
</script>

<style scoped>
.section {
  background: var(--color-background-secondary);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid var(--color-border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.section-info {
  flex: 1;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-foreground);
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 10px;
  color: var(--color-foreground-secondary);
  margin: 0;
}

.clear-all-btn {
  padding: 4px 8px;
  background: linear-gradient(135deg, #dc2626, #ef4444, #b91c1c);
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  font-size: 9px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  height: 24px;
  flex-shrink: 0;
}

.clear-all-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #b91c1c, #dc2626, #991b1b);
  transform: translateY(-1px);
}

.clear-all-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.loading, .empty {
  text-align: center;
  color: var(--color-foreground-secondary);
  padding: 24px;
  font-style: italic;
}

.custom-input-group {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.add-btn {
  padding: 6px 12px;
  background: linear-gradient(135deg, #6b7280, #9ca3af, #4b5563);
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  min-width: 50px;
  height: 32px;
}

.add-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4b5563, #6b7280, #374151);
  transform: translateY(-1px);
}

.add-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.custom-extensions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 160px;
  overflow-y: auto;
  padding-right: 6px;
}

.custom-extensions::-webkit-scrollbar {
  width: 6px;
}

.custom-extensions::-webkit-scrollbar-track {
  background: var(--color-background-secondary);
  border-radius: 3px;
}

.custom-extensions::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 3px;
}

.custom-extensions::-webkit-scrollbar-thumb:hover {
  background: var(--color-foreground-tertiary);
}

.custom-extension-tag {
  display: inline-flex;
  align-items: center;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 4px 8px;
  font-size: 10px;
  color: var(--color-foreground);
}

.tag-text {
  margin-right: 4px;
}

.remove-btn {
  background: none;
  border: none;
  color: var(--color-error);
  cursor: pointer;
  font-size: 12px;
  line-height: 1;
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.remove-btn:hover:not(:disabled) {
  background: var(--color-error);
  color: white;
}

.remove-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .section {
    padding: 24px;
    margin-bottom: 24px;
  }

  .custom-input-group {
    flex-direction: column;
  }

  .custom-extensions {
    max-height: 250px;
  }
}
</style>
