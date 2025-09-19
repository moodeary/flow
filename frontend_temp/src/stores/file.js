import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import ApiAxios from '@/api/ApiAxios.js'

/**
 * 파일 관리를 위한 Pinia 스토어
 * - 파일 업로드, 다운로드, 삭제, 목록 조회 기능 제공
 * - 업로드 진행 상황 관리
 * - 확장자 차단 여부 확인
 */
export const useFileStore = defineStore('file', () => {
  // ===== 상태 (State) =====
  const files = ref([])              // 업로드된 파일 목록
  const isLoadingFiles = ref(false)  // 파일 목록 로딩 상태
  const uploadingFiles = ref([])     // 업로드 진행 중인 파일들

  // 파일 목록 조회
  const loadFiles = async () => {
    isLoadingFiles.value = true
    try {
      const response = await ApiAxios.get('/api/files')
      if (response.data.success) {
        files.value = response.data.data
        return { success: true, data: response.data.data }
      }
      return { success: false, error: '파일 목록 조회 실패' }
    } catch (error) {
      console.error('파일 목록 조회 실패:', error)
      return { success: false, error: error.message }
    } finally {
      isLoadingFiles.value = false
    }
  }

  // 확장자 차단 여부 확인
  const checkFileExtension = async (extension) => {
    try {
      const response = await ApiAxios.get(`/api/extensions/check/${extension}`)
      return { success: true, isBlocked: response.data.data }
    } catch (error) {
      console.error('확장자 체크 실패:', error)
      return { success: false, isBlocked: false, error: error.message }
    }
  }

  // 파일 업로드
  const uploadFile = async (file) => {
    const uploadItem = {
      id: Date.now() + Math.random(),
      name: file.name,
      status: 'uploading',
      statusText: '업로드 중...'
    }

    uploadingFiles.value.push(uploadItem)

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await ApiAxios.post('/api/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      if (response.data.success) {
        uploadItem.status = 'success'
        uploadItem.statusText = '업로드 완료'

        // 파일 목록 새로고침
        await loadFiles()

        return { success: true, data: response.data.data }
      } else {
        uploadItem.status = 'error'
        uploadItem.statusText = '업로드 실패'
        return { success: false, error: '업로드 실패' }
      }
    } catch (error) {
      console.error('파일 업로드 실패:', error)
      uploadItem.status = 'error'
      uploadItem.statusText = '업로드 실패'
      return { success: false, error: error.message }
    } finally {
      // 3초 후 업로드 진행 목록에서 제거
      setTimeout(() => {
        const index = uploadingFiles.value.findIndex(item => item.id === uploadItem.id)
        if (index !== -1) {
          uploadingFiles.value.splice(index, 1)
        }
      }, 3000)
    }
  }

  // 파일 다운로드
  const downloadFile = async (file) => {
    try {
      const response = await ApiAxios.get(`/api/files/${file.id}/download`, {
        responseType: 'blob'
      })

      // Blob URL 생성 및 다운로드
      const blob = new Blob([response.data])
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = file.originalFilename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      return { success: true }
    } catch (error) {
      console.error('파일 다운로드 실패:', error)
      return { success: false, error: error.message }
    }
  }

  // 파일 삭제
  const deleteFile = async (file) => {
    try {
      const response = await ApiAxios.delete(`/api/files/${file.id}`)
      if (response.data.success) {
        await loadFiles() // 목록 새로고침
        return { success: true }
      } else {
        return { success: false, error: '파일 삭제 실패' }
      }
    } catch (error) {
      console.error('파일 삭제 실패:', error)
      return { success: false, error: error.message }
    }
  }

  // ===== Getters (계산된 속성) =====

  /**
   * 업로드된 파일 목록 (읽기 전용)
   */
  const getFiles = computed(() => files.value)

  /**
   * 파일 로딩 상태
   */
  const isFilesLoading = computed(() => isLoadingFiles.value)

  /**
   * 업로드 진행 중인 파일 목록
   */
  const getUploadingFiles = computed(() => uploadingFiles.value)

  /**
   * 전체 파일 개수
   */
  const getTotalFileCount = computed(() => files.value.length)

  /**
   * 업로드 중인 파일 개수
   */
  const getUploadingFileCount = computed(() => uploadingFiles.value.length)

  /**
   * 성공적으로 업로드 중인 파일들
   */
  const getSuccessfulUploads = computed(() =>
    uploadingFiles.value.filter(file => file.status === 'success')
  )

  /**
   * 실패한 업로드 파일들
   */
  const getFailedUploads = computed(() =>
    uploadingFiles.value.filter(file => file.status === 'error')
  )

  /**
   * 전체 파일 크기 합계 (바이트)
   */
  const getTotalFileSize = computed(() =>
    files.value.reduce((total, file) => total + (file.fileSize || 0), 0)
  )

  /**
   * 특정 확장자의 파일들만 필터링
   * @param {string} extension - 필터링할 확장자
   */
  const getFilesByExtension = computed(() => (extension) =>
    files.value.filter(file => {
      const fileExt = file.originalFilename?.split('.').pop()?.toLowerCase()
      return fileExt === extension.toLowerCase()
    })
  )

  // ===== 스토어 반환값 =====
  return {
    // 상태 (State) - 직접 접근용 (storeToRefs 사용 시)
    files,
    isLoadingFiles,
    uploadingFiles,

    // Getters (계산된 속성) - 권장 접근 방법
    getFiles,
    isFilesLoading,
    getUploadingFiles,
    getTotalFileCount,
    getUploadingFileCount,
    getSuccessfulUploads,
    getFailedUploads,
    getTotalFileSize,
    getFilesByExtension,

    // Actions (액션들)
    loadFiles,
    checkFileExtension,
    uploadFile,
    downloadFile,
    deleteFile
  }
})