import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ExtensionTest from '@/components/extensions/ExtensionTest.vue'

// Mock the store
const mockStore = {
  checkExtension: vi.fn(),
  getExtensionType: vi.fn(),
  unblockExtension: vi.fn(),
}

vi.mock('@/stores/extension', () => ({
  useExtensionStore: () => mockStore
}))

describe('ExtensionTest.vue', () => {
  let wrapper
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()

    wrapper = mount(ExtensionTest, {
      global: {
        plugins: [pinia]
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
  })

  describe('컴포넌트 렌더링', () => {
    it('기본 구조가 올바르게 렌더링됨', () => {
      expect(wrapper.find('.section-title').text()).toBe('파일 확장자 테스트')
      expect(wrapper.find('.section-desc').text()).toContain('파일명 또는 확장자를 입력하여 차단 상태를 확인하세요')
      expect(wrapper.find('.test-btn').text()).toBe('테스트')
      expect(wrapper.find('.test-btn').attributes('disabled')).toBeDefined()
    })

    it('입력 필드가 올바르게 렌더링됨', () => {
      const inputField = wrapper.findComponent({ name: 'InputField' })
      expect(inputField.exists()).toBe(true)
      expect(inputField.props('placeholder')).toContain('파일명 또는 확장자 입력')
      expect(inputField.props('maxlength')).toBe(20)
    })
  })

  describe('확장자 추출 함수', () => {
    it('파일명에서 확장자를 올바르게 추출', async () => {
      // 컴포넌트에 직접 접근할 수 없으므로 실제 동작을 테스트
      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('document.pdf')
      await wrapper.find('.test-btn').trigger('click')

      // checkExtension이 올바른 확장자로 호출되었는지 확인
      expect(mockStore.checkExtension).toHaveBeenCalledWith('pdf')
    })

    it('확장자만 입력했을 때 전체를 확장자로 처리', async () => {
      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('exe')
      await wrapper.find('.test-btn').trigger('click')

      expect(mockStore.checkExtension).toHaveBeenCalledWith('exe')
    })

    it('대소문자를 소문자로 변환', async () => {
      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('Document.PDF')
      await wrapper.find('.test-btn').trigger('click')

      expect(mockStore.checkExtension).toHaveBeenCalledWith('pdf')
    })

    it('여러 점이 있는 파일명에서 마지막 확장자만 추출', async () => {
      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('archive.tar.gz')
      await wrapper.find('.test-btn').trigger('click')

      expect(mockStore.checkExtension).toHaveBeenCalledWith('gz')
    })
  })

  describe('입력 유효성 검증', () => {
    it('빈 입력값에 대한 오류 메시지 표시', async () => {
      // 입력값이 비어있는 상태 설정
      wrapper.vm.testExtension = ''
      wrapper.vm.testInputError = '' // 초기 상태

      // checkExtension 함수 직접 호출 (버튼 클릭과 동일한 효과)
      await wrapper.vm.checkExtension()

      // 에러 메시지가 설정되었는지 확인
      expect(wrapper.vm.testInputError).toBe('파일명을 입력해주세요.')
      expect(mockStore.checkExtension).not.toHaveBeenCalled()
    })

    it('공백만 입력했을 때 오류 메시지 표시', async () => {
      // 공백 문자열 입력 상태 설정
      wrapper.vm.testExtension = '   '
      wrapper.vm.testInputError = '' // 초기 상태

      // checkExtension 함수 직접 호출
      await wrapper.vm.checkExtension()

      // 에러 메시지가 설정되었는지 확인
      expect(wrapper.vm.testInputError).toBe('파일명을 입력해주세요.')
      expect(mockStore.checkExtension).not.toHaveBeenCalled()
    })

    it('점으로 끝나는 파일명에 대한 오류 메시지 표시', async () => {
      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('file.')
      await wrapper.find('.test-btn').trigger('click')
      await wrapper.vm.$nextTick()

      expect(inputField.props('errorMessage')).toBe('올바른 파일명을 입력해주세요.')
      expect(mockStore.checkExtension).not.toHaveBeenCalled()
    })

    it('20자를 초과하는 확장자에 대한 오류 메시지 표시', async () => {
      const longExtension = 'a'.repeat(21)
      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue(`file.${longExtension}`)
      await wrapper.find('.test-btn').trigger('click')
      await wrapper.vm.$nextTick()

      expect(inputField.props('errorMessage')).toBe('확장자는 최대 20자까지 가능합니다.')
      expect(mockStore.checkExtension).not.toHaveBeenCalled()
    })
  })

  describe('확장자 테스트 기능', () => {
    it('차단된 확장자 테스트 결과 표시', async () => {
      mockStore.checkExtension.mockResolvedValue({
        success: true,
        isBlocked: true
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('virus.exe')
      await wrapper.find('.test-btn').trigger('click')

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.test-result.blocked').exists()).toBe(true)
      expect(wrapper.find('.result-title').text()).toBe('차단됨')
      expect(wrapper.find('.result-icon').text()).toBe('🚫')
      expect(wrapper.find('.result-filename').text()).toContain('virus.exe')
      expect(wrapper.find('.result-extension').text()).toContain('.exe')
      expect(wrapper.find('.unblock-btn').exists()).toBe(true)
    })

    it('허용된 확장자 테스트 결과 표시', async () => {
      mockStore.checkExtension.mockResolvedValue({
        success: true,
        isBlocked: false
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('image.jpg')
      await wrapper.find('.test-btn').trigger('click')

      await wrapper.vm.$nextTick()

      expect(wrapper.find('.test-result.allowed').exists()).toBe(true)
      expect(wrapper.find('.result-title').text()).toBe('허용됨')
      expect(wrapper.find('.result-icon').text()).toBe('✅')
      expect(wrapper.find('.result-filename').text()).toContain('image.jpg')
      expect(wrapper.find('.result-extension').text()).toContain('.jpg')
      expect(wrapper.find('.unblock-btn').exists()).toBe(false)
    })

    it('확장자 테스트 실패 시 오류 메시지 표시', async () => {
      mockStore.checkExtension.mockResolvedValue({
        success: false,
        error: '서버 오류'
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('test.ext')
      await wrapper.find('.test-btn').trigger('click')

      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent({ name: 'InputField' }).props('errorMessage')).toBe('서버 오류')
      expect(wrapper.find('.test-result').exists()).toBe(false)
    })

    it('네트워크 오류 시 적절한 오류 메시지 표시', async () => {
      mockStore.checkExtension.mockRejectedValue(new Error('네트워크 오류'))

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('test.ext')
      await wrapper.find('.test-btn').trigger('click')

      await wrapper.vm.$nextTick()

      expect(wrapper.findComponent({ name: 'InputField' }).props('errorMessage')).toBe('테스트에 실패했습니다. 서버를 확인해주세요.')
    })
  })

  describe('차단 해제 기능', () => {
    beforeEach(async () => {
      // 차단된 확장자 상태를 먼저 설정
      mockStore.checkExtension.mockResolvedValue({
        success: true,
        isBlocked: true
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('blocked.exe')
      await wrapper.find('.test-btn').trigger('click')
      await wrapper.vm.$nextTick()
    })

    it('고정 확장자 차단 해제 성공', async () => {
      mockStore.getExtensionType.mockResolvedValue({
        success: true,
        type: 'fixed'
      })
      mockStore.unblockExtension.mockResolvedValue({
        success: true
      })

      // confirm 설정 (setup.js에서 이미 모킹됨)
      confirm.mockReturnValue(true)

      await wrapper.find('.unblock-btn').trigger('click')
      await wrapper.vm.$nextTick()

      expect(mockStore.getExtensionType).toHaveBeenCalledWith('exe')
      expect(mockStore.unblockExtension).toHaveBeenCalledWith('exe', 'fixed')
      expect(wrapper.find('.result-title').text()).toBe('허용됨')
    })

    it('커스텀 확장자 차단 해제 성공', async () => {
      mockStore.getExtensionType.mockResolvedValue({
        success: true,
        type: 'custom'
      })
      mockStore.unblockExtension.mockResolvedValue({
        success: true
      })

      confirm.mockReturnValue(true)

      await wrapper.find('.unblock-btn').trigger('click')
      await wrapper.vm.$nextTick()

      expect(mockStore.getExtensionType).toHaveBeenCalledWith('exe')
      expect(mockStore.unblockExtension).toHaveBeenCalledWith('exe', 'custom')
    })

    it('차단 해제 확인 대화상자에서 취소', async () => {
      confirm.mockReturnValue(false)

      await wrapper.find('.unblock-btn').trigger('click')

      expect(mockStore.getExtensionType).not.toHaveBeenCalled()
      expect(mockStore.unblockExtension).not.toHaveBeenCalled()
    })

    it('차단 해제 실패 시 알림 표시', async () => {
      mockStore.getExtensionType.mockResolvedValue({
        success: true,
        type: 'fixed'
      })
      mockStore.unblockExtension.mockResolvedValue({
        success: false,
        error: '차단 해제 실패'
      })

      confirm.mockReturnValue(true)
      // alert은 setup.js에서 이미 모킹됨

      await wrapper.find('.unblock-btn').trigger('click')

      expect(alert).toHaveBeenCalledWith('차단 해제 실패')
    })
  })

  describe('로딩 상태', () => {
    it('테스트 중일 때 버튼 비활성화 및 텍스트 변경', async () => {
      // 느린 응답을 시뮬레이션
      let resolveCheck
      const checkPromise = new Promise(resolve => {
        resolveCheck = resolve
      })
      mockStore.checkExtension.mockReturnValue(checkPromise)

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('test.ext')
      const testButton = wrapper.find('.test-btn')

      // 클릭 이벤트 시작
      testButton.trigger('click')
      await wrapper.vm.$nextTick()

      // 로딩 상태 확인
      expect(wrapper.vm.isChecking).toBe(true)
      expect(testButton.text()).toBe('확인 중...')

      // 응답 완료
      resolveCheck({ success: true, isBlocked: false })
      await checkPromise // Promise 완료 대기
      await wrapper.vm.$nextTick()

      // 로딩 완료 상태 확인
      expect(wrapper.vm.isChecking).toBe(false)
      expect(testButton.text()).toBe('테스트')
    })

    it('차단 해제 중일 때 버튼 비활성화 및 텍스트 변경', async () => {
      // 먼저 차단된 상태 설정
      mockStore.checkExtension.mockResolvedValue({
        success: true,
        isBlocked: true
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('blocked.exe')
      await wrapper.find('.test-btn').trigger('click')
      await wrapper.vm.$nextTick()

      // 느린 차단 해제 응답 시뮬레이션
      let resolveUnblock
      const unblockPromise = new Promise(resolve => {
        resolveUnblock = resolve
      })

      // getExtensionType은 먼저 완료되어야 함
      mockStore.getExtensionType.mockResolvedValue({
        success: true,
        type: 'fixed'
      })

      // unblockExtension은 느리게 응답
      mockStore.unblockExtension.mockReturnValue(unblockPromise)

      confirm.mockReturnValue(true)

      const unblockButton = wrapper.find('.unblock-btn')

      // 차단 해제 버튼 클릭 및 getExtensionType 완료 대기
      const clickPromise = unblockButton.trigger('click')
      await wrapper.vm.$nextTick()

      // 로딩 상태 확인 (getExtensionType이 완료된 후)
      expect(unblockButton.text()).toBe('해제 중...')

      // unblockExtension 응답 완료
      resolveUnblock({ success: true })
      await clickPromise // 클릭 이벤트 처리 완료 대기
      await unblockPromise // Promise 완료 대기
      await wrapper.vm.$nextTick()

      // 결과 상태가 업데이트되어 버튼 텍스트가 변경됨
      expect(wrapper.find('.result-title').text()).toBe('허용됨')
    })
  })

  describe('이벤트 발생', () => {
    it('차단 해제 성공 시 extension-unblocked 이벤트 발생', async () => {
      mockStore.checkExtension.mockResolvedValue({
        success: true,
        isBlocked: true
      })
      mockStore.getExtensionType.mockResolvedValue({
        success: true,
        type: 'custom'
      })
      mockStore.unblockExtension.mockResolvedValue({
        success: true
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('test.custom')
      await wrapper.find('.test-btn').trigger('click')
      await wrapper.vm.$nextTick()

      confirm.mockReturnValue(true)

      await wrapper.find('.unblock-btn').trigger('click')
      await wrapper.vm.$nextTick()

      const emittedEvents = wrapper.emitted('extension-unblocked')
      expect(emittedEvents).toHaveLength(1)
      expect(emittedEvents[0][0]).toEqual({
        extension: 'custom',
        type: 'custom'
      })
    })
  })

  describe('키보드 이벤트', () => {
    it('Enter 키 입력 시 테스트 실행', async () => {
      mockStore.checkExtension.mockResolvedValue({
        success: true,
        isBlocked: false
      })

      const inputField = wrapper.findComponent({ name: 'InputField' })
      await inputField.setValue('test.jpg')
      await inputField.vm.$emit('enter')
      await wrapper.vm.$nextTick()

      expect(mockStore.checkExtension).toHaveBeenCalledWith('jpg')
    })
  })
})