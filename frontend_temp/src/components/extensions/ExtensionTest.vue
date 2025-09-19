<template>
  <div class="section">
    <h2 class="section-title">파일 확장자 테스트</h2>
    <p class="section-desc">파일명 또는 확장자를 입력하여 차단 상태를 확인하세요. (.이 없으면 전체를 확장자로 검사)</p>

    <div class="test-input-group">
      <InputField
        v-model="testExtension"
        placeholder="파일명 또는 확장자 입력 (예: document.pdf, exe, script.sh)"
        :maxlength="20"
        :error-message="testInputError"
        @enter="checkExtension"
      />
      <button
        class="test-btn"
        :disabled="!testExtension.trim() || isChecking"
        @click="checkExtension"
      >
        {{ isChecking ? '확인 중...' : '테스트' }}
      </button>
    </div>

    <div v-if="testResult" class="test-result" :class="{ 'blocked': testResult.isBlocked, 'allowed': !testResult.isBlocked }">
      <div class="result-icon">
        {{ testResult.isBlocked ? '🚫' : '✅' }}
      </div>
      <div class="result-content">
        <h3 class="result-title">
          {{ testResult.isBlocked ? '차단됨' : '허용됨' }}
        </h3>
        <p class="result-filename">입력값: {{ testResult.filename }}</p>
        <p class="result-extension">검사한 확장자: {{ testResult.filename.includes('.') ? '.' + testResult.extension : testResult.extension }}</p>
        <p class="result-message">{{ testResult.message }}</p>
      </div>
      <div v-if="testResult.isBlocked" class="result-actions">
        <button
          class="unblock-btn"
          :disabled="isUnblocking"
          @click="unblockExtension"
        >
          {{ isUnblocking ? '해제 중...' : '차단 해제' }}
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import InputField from '@/components/common/InputField.vue'
import { useExtensionStore } from '@/stores/extension'

const emit = defineEmits(['extension-unblocked'])

// Pinia 스토어 사용
const extensionStore = useExtensionStore()

const testExtension = ref('')
const testInputError = ref('')
const isChecking = ref(false)
const isUnblocking = ref(false)
const testResult = ref(null)

/**
 * 파일명에서 확장자를 추출하는 함수
 * - 마지막 '.' 이후의 문자열을 확장자로 추출
 * - '.'이 없으면 전체 파일명을 확장자로 처리
 * - 빈 문자열이면 빈 문자열 반환
 */
const extractExtension = (filename) => {
  if (!filename || typeof filename !== 'string') {
    return ''
  }

  const trimmed = filename.trim()
  if (!trimmed) {
    return ''
  }

  const lastDotIndex = trimmed.lastIndexOf('.')

  // '.'이 없으면 전체 파일명을 확장자로 처리
  if (lastDotIndex === -1) {
    return trimmed.toLowerCase()
  }

  // 마지막이 '.'인 경우 빈 문자열 반환
  if (lastDotIndex === trimmed.length - 1) {
    return ''
  }

  return trimmed.substring(lastDotIndex + 1).toLowerCase()
}

/**
 * 입력된 파일명에서 확장자를 추출하여 차단 상태를 확인하는 함수
 * - 입력값 유효성 검증 후 서버에 확장자 체크 요청
 * - 테스트 결과를 화면에 표시하고 이력에 추가
 * - 고정/커스텀 확장자 여부와 차단 상태를 종합적으로 확인
 */
const checkExtension = async () => {
  const input = testExtension.value.trim()

  // 입력값 유효성 검증
  if (!input) {
    testInputError.value = '파일명을 입력해주세요.'
    return
  }

  // 파일명에서 확장자 추출
  const extension = extractExtension(input)

  if (!extension) {
    testInputError.value = '올바른 파일명을 입력해주세요.'
    return
  }

  if (extension.length > 20) {
    testInputError.value = '확장자는 최대 20자까지 가능합니다.'
    return
  }

  // 검사 시작 - 상태 초기화
  testInputError.value = ''
  isChecking.value = true
  testResult.value = null

  try {
    // 스토어를 통해 확장자 차단 상태 확인
    const result = await extensionStore.checkExtension(extension)

    if (result.success) {
      // 테스트 결과 객체 생성
      testResult.value = {
        filename: input, // 원본 파일명
        extension,       // 추출된 확장자
        isBlocked: result.isBlocked, // 차단 여부 (boolean)
        message: result.isBlocked ? '이 확장자는 차단되었습니다.' : '이 확장자는 허용됩니다.',
        timestamp: new Date() // 테스트 실행 시간
      }
    } else {
      testInputError.value = result.error || '테스트에 실패했습니다.'
    }
  } catch (error) {
    console.error('확장자 테스트 실패:', error)
    testInputError.value = '테스트에 실패했습니다. 서버를 확인해주세요.'
  } finally {
    isChecking.value = false
  }
}

/**
 * 차단된 확장자를 허용하는 함수
 */
const unblockExtension = async () => {
  if (!testResult.value || !testResult.value.extension) {
    return
  }

  const extension = testResult.value.extension
  if (!confirm(`"${extension}" 확장자의 차단을 해제하시겠습니까?`)) {
    return
  }

  isUnblocking.value = true

  try {
    // 먼저 확장자가 고정인지 커스텀인지 확인
    const typeResult = await extensionStore.getExtensionType(extension)

    if (!typeResult.success) {
      alert('확장자 타입 확인에 실패했습니다.')
      return
    }

    const extensionType = typeResult.type // 'fixed' 또는 'custom'

    // 스토어를 통해 확장자 차단 해제
    const unblockResult = await extensionStore.unblockExtension(extension, extensionType)

    if (unblockResult.success) {
      testResult.value.isBlocked = false
      if (extensionType === 'custom') {
        testResult.value.message = '커스텀 확장자가 삭제되어 차단이 해제되었습니다.'
      } else {
        testResult.value.message = '고정 확장자의 차단이 해제되었습니다.'
      }

      // 부모 컴포넌트에 확장자 목록 새로고침 요청
      emit('extension-unblocked', { extension, type: extensionType })
    } else {
      alert(unblockResult.error || '차단 해제에 실패했습니다.')
    }
  } catch (error) {
    console.error('차단 해제 요청 실패:', error)
    alert('차단 해제에 실패했습니다. 서버를 확인해주세요.')
  } finally {
    isUnblocking.value = false
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

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-foreground);
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 10px;
  color: var(--color-foreground-secondary);
  margin: 0 0 12px 0;
}

.test-input-group {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}

.test-btn {
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
  min-width: 60px;
  height: 32px;
}

.test-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #4b5563, #6b7280, #374151);
  transform: translateY(-1px);
}

.test-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.test-result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 10px;
  transition: all 0.3s ease;
}

.test-result.blocked {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 1px solid #fecaca;
}

.test-result.allowed {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid #bbf7d0;
}

.result-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.result-content {
  flex: 1;
}

.result-title {
  font-size: 12px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.test-result.blocked .result-title {
  color: #dc2626;
}

.test-result.allowed .result-title {
  color: #16a34a;
}

.result-filename {
  font-size: 10px;
  margin: 0 0 2px 0;
  color: var(--color-foreground);
  font-weight: 500;
}

.result-extension {
  font-size: 10px;
  margin: 0 0 4px 0;
  color: var(--color-foreground-secondary);
  font-family: monospace;
}

.result-message {
  font-size: 11px;
  margin: 0;
  color: var(--color-foreground-secondary);
}

.result-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.unblock-btn {
  padding: 4px 8px;
  background: linear-gradient(135deg, #16a34a, #22c55e, #15803d);
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 600;
  font-size: 10px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  min-width: 60px;
  height: 24px;
}

.unblock-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #15803d, #16a34a, #166534);
  transform: translateY(-1px);
}

.unblock-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}


@media (max-width: 768px) {
  .section {
    padding: 24px;
    margin-bottom: 24px;
  }

  .test-input-group {
    flex-direction: column;
  }

  .test-result {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

}
</style>
