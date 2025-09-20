import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useExtensionStore } from '@/stores/extension'
import ApiAxios from '@/api/ApiAxios.js'

// Mock ApiAxios
vi.mock('@/api/ApiAxios.js', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  }
}))

describe('Extension Store', () => {
  let store

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useExtensionStore()
    vi.clearAllMocks()
  })

  describe('초기 상태', () => {
    it('초기 상태가 올바르게 설정됨', () => {
      expect(store.fixedExtensions).toEqual([])
      expect(store.customExtensions).toEqual([])
      expect(store.isLoadingFixed).toBe(false)
      expect(store.isLoadingCustom).toBe(false)
    })

    it('computed 값들이 올바르게 계산됨', () => {
      expect(store.getFixedExtensions).toEqual([])
      expect(store.getCustomExtensions).toEqual([])
      expect(store.getTotalExtensionCount).toBe(0)
      expect(store.getBlockedExtensionCount).toBe(0)
    })
  })

  describe('고정 확장자 관련 기능', () => {
    describe('loadFixedExtensions', () => {
      it('고정 확장자 조회 성공', async () => {
        const mockData = [
          { id: 1, extension: 'exe', description: '실행 파일', blocked: true },
          { id: 2, extension: 'bat', description: '배치 파일', blocked: false }
        ]

        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: mockData }
        })

        const result = await store.loadFixedExtensions()

        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/fixed')
        expect(result.success).toBe(true)
        expect(store.fixedExtensions).toHaveLength(2)
        expect(store.fixedExtensions[0].isBlocked).toBe(true)
        expect(store.fixedExtensions[1].isBlocked).toBe(false)
      })

      it('고정 확장자 조회 실패', async () => {
        ApiAxios.get.mockRejectedValue(new Error('네트워크 오류'))

        const result = await store.loadFixedExtensions()

        expect(result.success).toBe(false)
        expect(result.error).toBe('네트워크 오류')
        expect(store.fixedExtensions).toEqual([])
      })

      it('로딩 상태가 올바르게 관리됨', async () => {
        ApiAxios.get.mockImplementation(() => {
          expect(store.isLoadingFixed).toBe(true)
          return Promise.resolve({ data: { success: true, data: [] } })
        })

        await store.loadFixedExtensions()

        expect(store.isLoadingFixed).toBe(false)
      })
    })

    describe('toggleFixedExtension', () => {
      it('고정 확장자 토글 성공', async () => {
        ApiAxios.put.mockResolvedValue({
          data: { success: true, data: { extension: 'exe', isBlocked: false } }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.toggleFixedExtension('exe', false)

        expect(ApiAxios.put).toHaveBeenCalledWith('/api/extensions/fixed', {
          extension: 'exe',
          isBlocked: false
        })
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/fixed')
      })

      it('고정 확장자 토글 실패', async () => {
        ApiAxios.put.mockRejectedValue(new Error('토글 실패'))

        const result = await store.toggleFixedExtension('exe', false)

        expect(result.success).toBe(false)
        expect(result.error).toBe('토글 실패')
      })
    })

    describe('addFixedExtension', () => {
      it('고정 확장자 추가 성공', async () => {
        const mockResponse = {
          data: { success: true, data: { id: 3, extension: 'doc', description: '문서 파일' } }
        }

        ApiAxios.post.mockResolvedValue(mockResponse)
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.addFixedExtension('doc')

        expect(ApiAxios.post).toHaveBeenCalledWith('/api/extensions/fixed', { extension: 'doc' })
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/fixed')
      })

      it('중복 고정 확장자 추가 실패', async () => {
        const error = {
          response: {
            status: 409,
            data: { message: '이미 존재하는 고정 확장자입니다.' }
          }
        }

        ApiAxios.post.mockRejectedValue(error)

        const result = await store.addFixedExtension('exe')

        expect(result.success).toBe(false)
        expect(result.error).toBe('이미 존재하는 고정 확장자입니다.')
      })
    })

    describe('deleteFixedExtension', () => {
      it('고정 확장자 삭제 성공', async () => {
        ApiAxios.delete.mockResolvedValue({
          data: { success: true }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.deleteFixedExtension(1)

        expect(ApiAxios.delete).toHaveBeenCalledWith('/api/extensions/fixed/1')
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/fixed')
      })
    })

    describe('resetFixedExtensions', () => {
      it('고정 확장자 초기화 성공', async () => {
        ApiAxios.post.mockResolvedValue({
          data: { success: true }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.resetFixedExtensions()

        expect(ApiAxios.post).toHaveBeenCalledWith('/api/extensions/fixed/reset')
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/fixed')
      })
    })
  })

  describe('커스텀 확장자 관련 기능', () => {
    describe('loadCustomExtensions', () => {
      it('커스텀 확장자 조회 성공', async () => {
        const mockData = [
          { id: 1, extension: 'custom1' },
          { id: 2, extension: 'custom2' }
        ]

        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: mockData }
        })

        const result = await store.loadCustomExtensions()

        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/custom')
        expect(result.success).toBe(true)
        expect(store.customExtensions).toEqual(mockData)
      })

      it('커스텀 확장자 조회 실패', async () => {
        ApiAxios.get.mockRejectedValue(new Error('네트워크 오류'))

        const result = await store.loadCustomExtensions()

        expect(result.success).toBe(false)
        expect(result.error).toBe('네트워크 오류')
        expect(store.customExtensions).toEqual([])
      })
    })

    describe('addCustomExtension', () => {
      it('커스텀 확장자 추가 성공', async () => {
        const mockResponse = {
          data: { success: true, data: { id: 1, extension: 'mycustom' } }
        }

        ApiAxios.post.mockResolvedValue(mockResponse)
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.addCustomExtension('mycustom')

        expect(ApiAxios.post).toHaveBeenCalledWith('/api/extensions/custom', { extension: 'mycustom' })
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/custom')
      })
    })

    describe('deleteCustomExtension', () => {
      it('커스텀 확장자 삭제 성공', async () => {
        ApiAxios.delete.mockResolvedValue({
          data: { success: true }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.deleteCustomExtension(1)

        expect(ApiAxios.delete).toHaveBeenCalledWith('/api/extensions/custom/1')
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/custom')
      })
    })

    describe('deleteAllCustomExtensions', () => {
      it('모든 커스텀 확장자 삭제 성공', async () => {
        ApiAxios.delete.mockResolvedValue({
          data: { success: true }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.deleteAllCustomExtensions()

        expect(ApiAxios.delete).toHaveBeenCalledWith('/api/extensions/custom/all')
        expect(result.success).toBe(true)
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/custom')
      })
    })
  })

  describe('확장자 테스트 관련 기능', () => {
    describe('checkExtension', () => {
      it('확장자 차단 상태 확인 - 차단됨', async () => {
        ApiAxios.get.mockResolvedValue({
          data: { data: true }
        })

        const result = await store.checkExtension('exe')

        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/check/exe')
        expect(result.success).toBe(true)
        expect(result.isBlocked).toBe(true)
      })

      it('확장자 차단 상태 확인 - 허용됨', async () => {
        ApiAxios.get.mockResolvedValue({
          data: { data: false }
        })

        const result = await store.checkExtension('jpg')

        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/check/jpg')
        expect(result.success).toBe(true)
        expect(result.isBlocked).toBe(false)
      })
    })

    describe('getExtensionType', () => {
      it('고정 확장자 타입 확인', async () => {
        ApiAxios.get.mockResolvedValue({
          data: { data: 'fixed' }
        })

        const result = await store.getExtensionType('exe')

        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/type/exe')
        expect(result.success).toBe(true)
        expect(result.type).toBe('fixed')
      })

      it('커스텀 확장자 타입 확인', async () => {
        ApiAxios.get.mockResolvedValue({
          data: { data: 'custom' }
        })

        const result = await store.getExtensionType('mycustom')

        expect(result.success).toBe(true)
        expect(result.type).toBe('custom')
      })
    })

    describe('unblockExtension', () => {
      it('커스텀 확장자 차단 해제 (삭제)', async () => {
        ApiAxios.delete.mockResolvedValue({
          data: { success: true }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.unblockExtension('mycustom', 'custom')

        expect(ApiAxios.delete).toHaveBeenCalledWith('/api/extensions/custom/extension/mycustom')
        expect(result.success).toBe(true)
        expect(result.type).toBe('custom')
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/custom')
      })

      it('고정 확장자 차단 해제 (토글)', async () => {
        ApiAxios.put.mockResolvedValue({
          data: { success: true }
        })
        ApiAxios.get.mockResolvedValue({
          data: { success: true, data: [] }
        })

        const result = await store.unblockExtension('exe', 'fixed')

        expect(ApiAxios.put).toHaveBeenCalledWith('/api/extensions/fixed', {
          extension: 'exe',
          isBlocked: false
        })
        expect(result.success).toBe(true)
        expect(result.type).toBe('fixed')
        expect(ApiAxios.get).toHaveBeenCalledWith('/api/extensions/fixed')
      })
    })
  })

  describe('Computed 속성들', () => {
    beforeEach(() => {
      // 테스트 데이터 설정
      store.fixedExtensions = [
        { id: 1, extension: 'exe', isBlocked: true },
        { id: 2, extension: 'bat', isBlocked: false },
        { id: 3, extension: 'cmd', isBlocked: true }
      ]
      store.customExtensions = [
        { id: 1, extension: 'custom1' },
        { id: 2, extension: 'custom2' }
      ]
    })

    it('getBlockedFixedExtensions가 올바르게 필터링됨', () => {
      expect(store.getBlockedFixedExtensions).toHaveLength(2)
      expect(store.getBlockedFixedExtensions.map(ext => ext.extension)).toEqual(['exe', 'cmd'])
    })

    it('getAllowedFixedExtensions가 올바르게 필터링됨', () => {
      expect(store.getAllowedFixedExtensions).toHaveLength(1)
      expect(store.getAllowedFixedExtensions[0].extension).toBe('bat')
    })

    it('getTotalExtensionCount가 올바르게 계산됨', () => {
      expect(store.getTotalExtensionCount).toBe(5)
    })

    it('getBlockedExtensionCount가 올바르게 계산됨', () => {
      expect(store.getBlockedExtensionCount).toBe(4) // 차단된 고정 2개 + 커스텀 2개
    })

    it('isExtensionBlocked이 올바르게 동작함', () => {
      expect(store.isExtensionBlocked('exe')).toBe(true) // 차단된 고정 확장자
      expect(store.isExtensionBlocked('bat')).toBe(false) // 허용된 고정 확장자
      expect(store.isExtensionBlocked('custom1')).toBe(true) // 커스텀 확장자
      expect(store.isExtensionBlocked('jpg')).toBe(false) // 등록되지 않은 확장자
      expect(store.isExtensionBlocked('EXE')).toBe(true) // 대소문자 구분 없음
    })
  })
})