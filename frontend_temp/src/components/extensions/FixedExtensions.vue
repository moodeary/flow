<template>
  <div class="section">
    <div class="section-header">
      <div class="section-info">
        <h2 class="section-title">고정 확장자</h2>
        <p class="section-desc">자주 차단하는 확장자 목록입니다. 최대 9개까지 추가할 수 있습니다. (현재: {{ fixedExtensions.length }}/9)</p>
      </div>
      <button
        v-if="fixedExtensions.length > 0"
        class="reset-btn"
        @click="resetFixedExtensions"
        :disabled="loading"
        title="기본 확장자로 초기화"
      >
        초기화
      </button>
    </div>

    <div class="fixed-input-group">
      <InputField
        v-model="newFixedExtension"
        placeholder="고정 확장자 입력 (예: exe, bat)"
        :maxlength="20"
        :show-counter="true"
        :error-message="fixedInputError"
        @enter="addFixedExtension"
      />
      <button
        class="add-btn"
        :disabled="!newFixedExtension.trim() || fixedExtensions.length >= 9 || loading"
        @click="addFixedExtension"
      >
        추가
      </button>
    </div>

    <div v-if="loading" class="loading">고정 확장자를 불러오는 중...</div>
    <div v-else-if="fixedExtensions.length === 0" class="empty">고정 확장자가 없습니다.</div>
    <div v-else class="fixed-extensions">
      <div
        v-for="extension in fixedExtensions"
        :key="extension.id"
        class="fixed-extension-item"
      >
        <div class="extension-content">
          <ToggleButton
            :model-value="extension.isBlocked"
            :label="extension.extension"
            @update:model-value="updateFixedExtension(extension.extension, $event)"
          />
          <button
            class="remove-fixed-btn"
            @click="removeFixedExtension(extension.id)"
            :disabled="loading"
            title="고정 확장자 삭제"
          >
            ×
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import InputField from '@/components/common/InputField.vue'
import ToggleButton from '@/components/common/ToggleButton.vue'
import { useExtensionStore } from '@/stores/extension'

const newFixedExtension = ref('')
const fixedInputError = ref('')

// Pinia 스토어 사용
const extensionStore = useExtensionStore()
const { fixedExtensions, isLoadingFixed: loading } = storeToRefs(extensionStore)

/**
 * 고정 확장자 목록을 서버에서 조회하는 함수
 */
const fetchFixedExtensions = async () => {
  console.log('🚀 fetchFixedExtensions 호출됨')
  const result = await extensionStore.loadFixedExtensions()
  console.log('🔍 fetchFixedExtensions 결과:', result)
  if (!result.success) {
    alert('고정 확장자 목록을 불러오는데 실패했습니다.')
  }
}

/**
 * 고정 확장자의 차단 상태를 토글하는 함수
 */
const updateFixedExtension = async (extension, isBlocked) => {
  const action = isBlocked ? '차단' : '허용'
  const confirmMessage = `"${extension}" 확장자를 ${action}하시겠습니까?`

  if (!confirm(confirmMessage)) {
    // 취소시 이전 상태로 되돌리기
    fetchFixedExtensions()
    return
  }

  const result = await extensionStore.toggleFixedExtension(extension, isBlocked)
  if (!result.success) {
    alert('고정 확장자 상태 변경에 실패했습니다.')
    // 실패 시 서버 데이터로 다시 동기화
    fetchFixedExtensions()
  }
}

/**
 * 고정 확장자 입력값의 유효성을 검증하는 함수
 * - 빈 값, 길이 제한, 중복 확인 등의 검증 수행
 * - 에러 메시지 반환 또는 빈 문자열(유효함) 반환
 */
const validateFixedExtension = (extension) => {
  if (!extension.trim()) {
    return '확장자를 입력해주세요.'
  }

  if (extension.length > 20) {
    return '확장자는 최대 20자까지 입력 가능합니다.'
  }

  // 고정 확장자 중복 확인
  if (fixedExtensions.value.some(ext => ext.extension === extension.trim())) {
    return '고정 확장자에 이미 있는 확장자입니다.'
  }

  return '' // 유효함
}

/**
 * 새로운 고정 확장자를 추가하는 함수
 */
const addFixedExtension = async () => {
  const extension = newFixedExtension.value.trim()
  const error = validateFixedExtension(extension)

  if (error) {
    fixedInputError.value = error
    return
  }

  // 최대 개수 제한 확인
  if (fixedExtensions.value.length >= 9) {
    fixedInputError.value = '최대 9개까지만 추가할 수 있습니다.'
    return
  }

  const result = await extensionStore.addFixedExtension(extension)
  if (result.success) {
    newFixedExtension.value = '' // 입력 폼 초기화
    fixedInputError.value = ''
  } else {
    fixedInputError.value = result.error || '추가에 실패했습니다.'
  }
}

/**
 * 고정 확장자를 삭제하는 함수
 */
const removeFixedExtension = async (id) => {
  const extension = fixedExtensions.value.find(ext => ext.id === id)
  if (!extension) return

  // 삭제 확인
  if (!confirm(`"${extension.extension}" 고정 확장자를 삭제하시겠습니까?`)) {
    return
  }

  const result = await extensionStore.deleteFixedExtension(id)
  if (!result.success) {
    alert('고정 확장자 삭제에 실패했습니다.')
  }
}

// 컴포넌트 마운트 시 초기 데이터 로드
onMounted(() => {
  fetchFixedExtensions() // 고정 확장자 목록 조회
})

/**
 * 고정 확장자를 기본 상태로 초기화하는 함수
 */
const resetFixedExtensions = async () => {
  if (!confirm('고정 확장자를 기본 상태(bat, cmd, cpl, exe, js, scr)로 초기화하시겠습니까?')) {
    return
  }

  const result = await extensionStore.resetFixedExtensions()
  if (!result.success) {
    alert('고정 확장자 초기화에 실패했습니다.')
  }
}

// 부모 컴포넌트에서 호출할 수 있는 메서드 expose
defineExpose({
  loadFixedExtensions: fetchFixedExtensions
})
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

.reset-btn {
  padding: 4px 8px;
  background: linear-gradient(135deg, #f59e0b, #fbbf24, #d97706);
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

.reset-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #d97706, #f59e0b, #b45309);
  transform: translateY(-1px);
}

.reset-btn:disabled {
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

.fixed-input-group {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.add-btn {
  height: 32px;
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

.fixed-extensions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  max-height: 130px;
  overflow-y: auto;
  padding-right: 6px;
}

.fixed-extensions::-webkit-scrollbar {
  width: 6px;
}

.fixed-extensions::-webkit-scrollbar-track {
  background: var(--color-background-secondary);
  border-radius: 3px;
}

.fixed-extensions::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: 3px;
}

.fixed-extensions::-webkit-scrollbar-thumb:hover {
  background: var(--color-foreground-tertiary);
}

.fixed-extension-item {
  padding: 6px 2px;
  background: var(--color-background);
  border-radius: 6px;
  border: 1px solid var(--color-border);
}

.extension-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.remove-fixed-btn {
  background: none;
  border: none;
  color: var(--color-error);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  margin-left: 6px;
  flex-shrink: 0;
}

.remove-fixed-btn:hover:not(:disabled) {
  background: var(--color-error);
  color: white;
}

.remove-fixed-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .section {
    padding: 24px;
    margin-bottom: 24px;
  }

  .fixed-input-group {
    flex-direction: column;
  }

  .fixed-extensions {
    grid-template-columns: repeat(3, 1fr);
    max-height: 250px;
  }
}

@media (max-width: 480px) {
  .fixed-extensions {
    grid-template-columns: 1fr;
  }
}
</style>
