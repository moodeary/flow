import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import ApiAxios from '@/api/ApiAxios.js'

/**
 * 확장자 관리를 위한 Pinia 스토어
 * - 고정 확장자와 커스텀 확장자의 CRUD 작업 처리
 * - 확장자 테스트 및 차단 해제 기능 제공
 * - 모든 API 호출과 상태 관리를 중앙화
 */
export const useExtensionStore = defineStore('extension', () => {
  // ===== 상태 (State) =====
  const fixedExtensions = ref([])        // 고정 확장자 목록
  const customExtensions = ref([])       // 커스텀 확장자 목록
  const isLoadingFixed = ref(false)      // 고정 확장자 로딩 상태
  const isLoadingCustom = ref(false)     // 커스텀 확장자 로딩 상태

  // ===== 고정 확장자 관련 액션 =====

  /**
   * 고정 확장자 목록을 서버에서 조회
   * - GET /api/extensions/fixed 호출
   * - 시스템에서 기본 제공하는 확장자들 (bat, cmd, exe 등)
   * - blocked 필드를 isBlocked로 매핑하여 저장
   * @returns {Object} { success: boolean, data?: Array, error?: string }
   */
  const loadFixedExtensions = async () => {
    isLoadingFixed.value = true
    try {
      const response = await ApiAxios.get('/api/extensions/fixed')
      console.log('🔍 loadFixedExtensions 응답:', response.data)
      if (response.data.success) {
        // 서버에서 blocked 필드로 받아서 isBlocked로 매핑
        const mappedData = response.data.data.map(ext => ({
          ...ext,
          isBlocked: ext.blocked ?? false // blocked 필드를 isBlocked로 매핑
        }))
        console.log('🔍 매핑된 고정 확장자 데이터:', mappedData)
        fixedExtensions.value = mappedData
        return { success: true, data: response.data.data }
      }
      return { success: false, error: '고정 확장자 조회 실패' }
    } catch (error) {
      console.error('고정 확장자 조회 실패:', error)
      return { success: false, error: error.message }
    } finally {
      isLoadingFixed.value = false
    }
  }

  /**
   * 고정 확장자의 차단 상태를 토글 (차단 ↔ 허용)
   * - PUT /api/extensions/fixed/{extension} 호출
   * - 토글 버튼 클릭 시 사용되며 확인 대화상자는 컴포넌트에서 처리
   * - 성공 시 목록을 새로고침하여 최신 상태 반영
   * @param {string} extension - 토글할 확장자명
   * @param {boolean} isBlocked - 설정할 차단 상태
   * @returns {Object} { success: boolean, data?: Object, error?: string }
   */
  const toggleFixedExtension = async (extension, isBlocked) => {
    try {
      const response = await ApiAxios.put('/api/extensions/fixed', {
        extension: extension,
        isBlocked: isBlocked
      })
      console.log('🔄 고정확장자 토글 응답:', response.data)
      if (response.data.success) {
        await loadFixedExtensions()
        return { success: true, data: response.data.data }
      }
      return { success: false, error: '고정 확장자 토글 실패' }
    } catch (error) {
      console.error('고정 확장자 토글 실패:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 새로운 고정 확장자를 추가
   * - POST /api/extensions/fixed 호출
   * - 사용자가 입력한 확장자를 고정 확장자 목록에 추가
   * - 최대 9개까지 제한 (유효성 검증은 컴포넌트에서 처리)
   * - 성공 시 목록을 새로고침하여 추가된 항목 반영
   * @param {string} extension - 추가할 확장자명
   * @returns {Object} { success: boolean, data?: Object, error?: string }
   */
  const addFixedExtension = async (extension) => {
    try {
      const response = await ApiAxios.post('/api/extensions/fixed', { extension })
      if (response.data.success) {
        await loadFixedExtensions()
        return { success: true, data: response.data.data }
      }
      return { success: false, error: '고정 확장자 추가 실패' }
    } catch (error) {
      console.error('고정 확장자 추가 실패:', error)

      // 409 Conflict 에러인 경우 백엔드에서 제공하는 친화적인 메시지 사용
      if (error.response && error.response.status === 409) {
        const message = error.response.data?.message || '중복된 확장자입니다.'
        return { success: false, error: message }
      }

      return { success: false, error: error.message }
    }
  }

  /**
   * 고정 확장자를 삭제
   * - DELETE /api/extensions/fixed/{id} 호출
   * - 사용자가 추가한 고정 확장자를 개별 삭제
   * - 삭제 확인 대화상자는 컴포넌트에서 처리
   * - 성공 시 목록을 새로고침하여 삭제된 항목 제거
   * @param {number} id - 삭제할 확장자의 ID
   * @returns {Object} { success: boolean, error?: string }
   */
  const deleteFixedExtension = async (id) => {
    try {
      const response = await ApiAxios.delete(`/api/extensions/fixed/${id}`)
      if (response.data.success) {
        await loadFixedExtensions()
        return { success: true }
      }
      return { success: false, error: '고정 확장자 삭제 실패' }
    } catch (error) {
      console.error('고정 확장자 삭제 실패:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 고정 확장자를 기본 상태로 초기화
   * - POST /api/extensions/fixed/reset 호출
   * - 기본 6개 확장자(bat, cmd, cpl, exe, js, scr)만 남기고 나머지 삭제
   * - 초기화 확인 대화상자는 컴포넌트에서 처리
   * - 성공 시 목록을 새로고침하여 초기화된 상태 반영
   * @returns {Object} { success: boolean, error?: string }
   */
  const resetFixedExtensions = async () => {
    try {
      const response = await ApiAxios.post('/api/extensions/fixed/reset')
      if (response.data.success) {
        await loadFixedExtensions()
        return { success: true }
      }
      return { success: false, error: '고정 확장자 초기화 실패' }
    } catch (error) {
      console.error('고정 확장자 초기화 실패:', error)
      return { success: false, error: error.message }
    }
  }

  // ===== 커스텀 확장자 관련 액션 =====

  /**
   * 커스텀 확장자 목록을 서버에서 조회
   * - GET /api/extensions/custom 호출
   * - 사용자가 직접 추가한 확장자들의 목록 조회
   * - 고정 확장자와 달리 항상 차단 상태 (토글 불가)
   * @returns {Object} { success: boolean, data?: Array, error?: string }
   */
  const loadCustomExtensions = async () => {
    isLoadingCustom.value = true
    try {
      const response = await ApiAxios.get('/api/extensions/custom')
      console.log('🔍 loadCustomExtensions 응답:', response.data)
      if (response.data.success) {
        customExtensions.value = response.data.data
        console.log('🔍 커스텀 확장자 데이터:', response.data.data)
        return { success: true, data: response.data.data }
      }
      return { success: false, error: '커스텀 확장자 조회 실패' }
    } catch (error) {
      console.error('커스텀 확장자 조회 실패:', error)
      return { success: false, error: error.message }
    } finally {
      isLoadingCustom.value = false
    }
  }

  /**
   * 새로운 커스텀 확장자를 추가
   * - POST /api/extensions/custom 호출
   * - 사용자가 입력한 확장자를 커스텀 확장자 목록에 추가
   * - 최대 200개까지 제한 (유효성 검증은 컴포넌트에서 처리)
   * - 추가된 확장자는 즉시 차단 상태가 됨
   * - 성공 시 목록을 새로고침하여 추가된 항목 반영
   * @param {string} extension - 추가할 확장자명
   * @returns {Object} { success: boolean, data?: Object, error?: string }
   */
  const addCustomExtension = async (extension) => {
    try {
      const response = await ApiAxios.post('/api/extensions/custom', { extension })
      if (response.data.success) {
        await loadCustomExtensions()
        return { success: true, data: response.data.data }
      }
      return { success: false, error: '커스텀 확장자 추가 실패' }
    } catch (error) {
      console.error('커스텀 확장자 추가 실패:', error)

      // 409 Conflict 에러인 경우 백엔드에서 제공하는 친화적인 메시지 사용
      if (error.response && error.response.status === 409) {
        const message = error.response.data?.message || '중복된 확장자입니다.'
        return { success: false, error: message }
      }

      return { success: false, error: error.message }
    }
  }

  /**
   * 커스텀 확장자를 삭제
   * - DELETE /api/extensions/custom/{id} 호출
   * - 지정된 ID의 커스텀 확장자를 개별 삭제
   * - 삭제 확인 대화상자는 컴포넌트에서 처리
   * - 성공 시 목록을 새로고침하여 삭제된 항목 제거
   * @param {number} id - 삭제할 확장자의 ID
   * @returns {Object} { success: boolean, error?: string }
   */
  const deleteCustomExtension = async (id) => {
    try {
      const response = await ApiAxios.delete(`/api/extensions/custom/${id}`)
      if (response.data.success) {
        await loadCustomExtensions()
        return { success: true }
      }
      return { success: false, error: '커스텀 확장자 삭제 실패' }
    } catch (error) {
      console.error('커스텀 확장자 삭제 실패:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 모든 커스텀 확장자를 일괄 삭제
   * - DELETE /api/extensions/custom/all 호출
   * - 사용자가 추가한 모든 커스텀 확장자를 한 번에 삭제
   * - 전체 삭제 확인 대화상자는 컴포넌트에서 처리
   * - 성공 시 목록을 새로고침하여 빈 상태로 만듦
   * @returns {Object} { success: boolean, error?: string }
   */
  const deleteAllCustomExtensions = async () => {
    try {
      const response = await ApiAxios.delete('/api/extensions/custom/all')
      if (response.data.success) {
        await loadCustomExtensions()
        return { success: true }
      }
      return { success: false, error: '커스텀 확장자 전체 삭제 실패' }
    } catch (error) {
      console.error('커스텀 확장자 전체 삭제 실패:', error)
      return { success: false, error: error.message }
    }
  }

  // ===== 확장자 테스트 관련 액션 =====

  /**
   * 특정 확장자의 차단 상태를 확인
   * - GET /api/extensions/check/{extension} 호출
   * - 확장자 테스트 기능에서 사용
   * - 고정/커스텀 확장자 모두를 종합하여 차단 여부 판단
   * @param {string} extension - 확인할 확장자명
   * @returns {Object} { success: boolean, isBlocked?: boolean, error?: string }
   */
  const checkExtension = async (extension) => {
    try {
      const response = await ApiAxios.get(`/api/extensions/check/${extension}`)
      return { success: true, isBlocked: response.data.data }
    } catch (error) {
      console.error('확장자 체크 실패:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 확장자의 타입(고정/커스텀)을 확인
   * - GET /api/extensions/type/{extension} 호출
   * - 차단 해제 시 어떤 방식으로 처리할지 결정하기 위해 사용
   * - 고정 확장자: 토글로 차단 해제, 커스텀 확장자: 삭제로 차단 해제
   * @param {string} extension - 타입을 확인할 확장자명
   * @returns {Object} { success: boolean, type?: string, error?: string }
   */
  const getExtensionType = async (extension) => {
    try {
      const response = await ApiAxios.get(`/api/extensions/type/${extension}`)
      return { success: true, type: response.data.data }
    } catch (error) {
      console.error('확장자 타입 조회 실패:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 차단된 확장자의 차단을 해제
   * - 커스텀 확장자: DELETE /api/extensions/custom/extension/{extension} 호출 (삭제)
   * - 고정 확장자: PUT /api/extensions/fixed 호출 (enabled: false로 토글)
   * - 확장자 테스트에서 차단 해제 버튼 클릭 시 사용
   * - 성공 시 해당 타입의 확장자 목록을 새로고침
   * @param {string} extension - 차단 해제할 확장자명
   * @param {string} type - 확장자 타입 ('fixed' 또는 'custom')
   * @returns {Object} { success: boolean, type?: string, error?: string }
   */
  const unblockExtension = async (extension, type) => {
    try {
      let response
      if (type === 'custom') {
        response = await ApiAxios.delete(`/api/extensions/custom/extension/${extension}`)
      } else if (type === 'fixed') {
        response = await ApiAxios.put('/api/extensions/fixed', {
          extension: extension,
          isBlocked: false
        })
      }

      if (response.data.success) {
        // 해당 타입의 확장자 목록 새로고침
        if (type === 'custom') {
          await loadCustomExtensions()
        } else if (type === 'fixed') {
          await loadFixedExtensions()
        }
        return { success: true, type }
      }
      return { success: false, error: '확장자 차단 해제 실패' }
    } catch (error) {
      console.error('확장자 차단 해제 실패:', error)
      return { success: false, error: error.message }
    }
  }

  // ===== Getters (계산된 속성) =====

  /**
   * 고정 확장자 목록 (읽기 전용)
   * - reactive한 고정 확장자 배열 반환
   * - 컴포넌트에서 직접 접근하는 대신 이 getter 사용 권장
   */
  const getFixedExtensions = computed(() => fixedExtensions.value)

  /**
   * 커스텀 확장자 목록 (읽기 전용)
   * - reactive한 커스텀 확장자 배열 반환
   * - 컴포넌트에서 직접 접근하는 대신 이 getter 사용 권장
   */
  const getCustomExtensions = computed(() => customExtensions.value)

  /**
   * 고정 확장자 로딩 상태
   */
  const isFixedLoading = computed(() => isLoadingFixed.value)

  /**
   * 커스텀 확장자 로딩 상태
   */
  const isCustomLoading = computed(() => isLoadingCustom.value)

  /**
   * 차단된 고정 확장자 목록
   * - isBlocked가 true인 고정 확장자들만 필터링
   */
  const getBlockedFixedExtensions = computed(() =>
    fixedExtensions.value.filter(ext => ext.isBlocked)
  )

  /**
   * 허용된 고정 확장자 목록
   * - isBlocked가 false인 고정 확장자들만 필터링
   */
  const getAllowedFixedExtensions = computed(() =>
    fixedExtensions.value.filter(ext => !ext.isBlocked)
  )

  /**
   * 전체 확장자 개수 (고정 + 커스텀)
   */
  const getTotalExtensionCount = computed(() =>
    fixedExtensions.value.length + customExtensions.value.length
  )

  /**
   * 차단된 전체 확장자 개수
   */
  const getBlockedExtensionCount = computed(() =>
    getBlockedFixedExtensions.value.length + customExtensions.value.length
  )

  /**
   * 특정 확장자가 차단되어 있는지 확인
   * @param {string} extension - 확인할 확장자명
   * @returns {boolean} 차단 여부
   */
  const isExtensionBlocked = computed(() => (extension) => {
    const lowerExt = extension.toLowerCase()

    // 고정 확장자에서 차단된 것이 있는지 확인
    const blockedInFixed = fixedExtensions.value.some(
      ext => ext.extension.toLowerCase() === lowerExt && ext.isBlocked
    )

    // 커스텀 확장자에 있는지 확인 (커스텀은 모두 차단)
    const blockedInCustom = customExtensions.value.some(
      ext => ext.extension.toLowerCase() === lowerExt
    )

    return blockedInFixed || blockedInCustom
  })

  // ===== 스토어 반환값 =====
  return {
    // 상태 (State) - 직접 접근용 (storeToRefs 사용 시)
    fixedExtensions,
    customExtensions,
    isLoadingFixed,
    isLoadingCustom,

    // Getters (계산된 속성) - 권장 접근 방법
    getFixedExtensions,
    getCustomExtensions,
    isFixedLoading,
    isCustomLoading,
    getBlockedFixedExtensions,
    getAllowedFixedExtensions,
    getTotalExtensionCount,
    getBlockedExtensionCount,
    isExtensionBlocked,

    // 고정 확장자 액션들
    loadFixedExtensions,    // 고정 확장자 목록 조회
    toggleFixedExtension,   // 고정 확장자 차단/허용 토글
    addFixedExtension,      // 고정 확장자 추가
    deleteFixedExtension,   // 고정 확장자 삭제
    resetFixedExtensions,   // 고정 확장자 초기화

    // 커스텀 확장자 액션들
    loadCustomExtensions,     // 커스텀 확장자 목록 조회
    addCustomExtension,       // 커스텀 확장자 추가
    deleteCustomExtension,    // 커스텀 확장자 삭제
    deleteAllCustomExtensions, // 커스텀 확장자 전체 삭제

    // 확장자 테스트 액션들
    checkExtension,         // 확장자 차단 상태 확인
    getExtensionType,       // 확장자 타입 확인 (fixed/custom)
    unblockExtension        // 확장자 차단 해제
  }
})