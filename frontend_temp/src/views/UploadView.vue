<template>
  <div class="upload-container">
    <div class="header">
      <h1 class="title">파일 업로드 및 관리</h1>
      <p class="description">파일을 업로드하고 다운로드할 수 있습니다.</p>
    </div>

    <div class="main-content">
      <div class="upload-section">
        <h2 class="section-title">파일 업로드</h2>
        <p class="section-desc">파일을 드래그하거나 클릭하여 업로드하세요. 확장자 차단 기능이 적용됩니다.</p>

      <!-- 파일 업로드 영역 -->
      <div class="upload-area"
           :class="{ 'drag-over': isDragOver }"
           @drop="handleDrop"
           @dragover.prevent
           @dragenter="isDragOver = true"
           @dragleave="isDragOver = false"
           @click="triggerFileInput">
        <input ref="fileInput" type="file" multiple style="display: none" @change="handleFileSelect">
        <div class="upload-content">
          <div class="upload-icon">📁</div>
          <p class="upload-text">파일을 드래그하거나 클릭하여 선택하세요</p>
          <p class="upload-hint">여러 파일 동시 선택 가능 (최대 10MB)</p>
        </div>
      </div>

      <!-- 업로드 진행 상황 -->
      <div v-if="uploadingFiles.length > 0" class="upload-progress">
        <h3>업로드 진행 상황</h3>
        <div v-for="file in uploadingFiles" :key="file.id" class="upload-item">
          <span class="upload-filename">{{ file.name }}</span>
          <div class="upload-status" :class="file.status">
            {{ file.statusText }}
          </div>
        </div>
        </div>
      </div>

      <div class="files-section">
        <h2 class="section-title">업로드된 파일 목록</h2>
        <p class="section-desc">업로드된 파일들을 다운로드하거나 삭제할 수 있습니다.</p>

      <div v-if="isLoadingFiles" class="loading">파일 목록을 불러오는 중...</div>
      <div v-else-if="files.length === 0" class="empty">업로드된 파일이 없습니다.</div>
      <div v-else class="files-table">
        <div class="table-header">
          <span>파일명</span>
          <span>크기</span>
          <span>업로드일</span>
          <span>작업</span>
        </div>
        <div v-for="file in files" :key="file.id" class="table-row">
          <div class="file-info">
            <div class="file-name">{{ file.originalFilename }}</div>
            <div class="file-extension">{{ getFileExtension(file.originalFilename) }}</div>
          </div>
          <div class="file-size">{{ formatFileSize(file.fileSize) }}</div>
          <div class="file-date">{{ formatDate(file.createdAt) }}</div>
          <div class="file-actions">
            <button class="action-btn download" @click="downloadFile(file)" title="다운로드">
              📥
            </button>
            <button class="action-btn delete" @click="deleteFile(file)" title="삭제">
              🗑️
            </button>
          </div>
        </div>
      </div>

      <button v-if="files.length > 0" class="refresh-btn" @click="fileStore.loadFiles">
        새로고침
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useFileStore } from '@/stores/file'

// 파일 업로드 관련
const fileInput = ref(null)
const isDragOver = ref(false)

// Pinia 스토어 사용
const fileStore = useFileStore()
const { files, isLoadingFiles, uploadingFiles } = storeToRefs(fileStore)


// 파일 업로드/다운로드 관련 함수들

/**
 * 파일 입력 창을 트리거하는 함수
 */
const triggerFileInput = () => {
  fileInput.value?.click()
}

/**
 * 드래그 앤 드롭으로 파일이 떨어졌을 때 처리
 */
const handleDrop = (event) => {
  event.preventDefault()
  isDragOver.value = false

  const droppedFiles = Array.from(event.dataTransfer.files)
  processFiles(droppedFiles)
}

/**
 * 파일 선택으로 파일이 선택되었을 때 처리
 */
const handleFileSelect = (event) => {
  const selectedFiles = Array.from(event.target.files)
  processFiles(selectedFiles)
  // 파일 입력 초기화
  event.target.value = ''
}

/**
 * 선택된 파일들을 처리하는 함수
 */
const processFiles = async (fileList) => {
  for (const file of fileList) {
    if (file.size > 10 * 1024 * 1024) { // 10MB 제한
      alert(`${file.name}은 10MB를 초과합니다.`)
      continue
    }

    // 확장자 체크
    const extension = getFileExtension(file.name)
    const extensionResult = await fileStore.checkFileExtension(extension)

    if (!extensionResult.success) {
      console.error('확장자 체크 실패:', extensionResult.error)
      continue
    }

    if (extensionResult.isBlocked) {
      alert(`${file.name}의 확장자(${extension})는 차단된 확장자입니다.`)
      continue
    }

    // 업로드 진행
    const uploadResult = await fileStore.uploadFile(file)
    if (!uploadResult.success) {
      console.error('업로드 실패:', uploadResult.error)
    }
  }
}

/**
 * 파일 다운로드
 */
const downloadFile = async (file) => {
  const result = await fileStore.downloadFile(file)
  if (!result.success) {
    alert('파일 다운로드에 실패했습니다.')
  }
}

/**
 * 파일 삭제
 */
const deleteFile = async (file) => {
  if (!confirm(`${file.originalFilename}을 삭제하시겠습니까?`)) {
    return
  }

  const result = await fileStore.deleteFile(file)
  if (!result.success) {
    alert('파일 삭제에 실패했습니다.')
  }
}

/**
 * 파일 확장자 추출
 */
const getFileExtension = (filename) => {
  const lastDotIndex = filename.lastIndexOf('.')
  if (lastDotIndex === -1) return ''
  return filename.substring(lastDotIndex + 1).toLowerCase()
}

/**
 * 파일 크기 포맷팅
 */
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 날짜 포맷팅
 */
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

// 컴포넌트 마운트 시 파일 목록 로드
onMounted(() => {
  fileStore.loadFiles()
})
</script>

<style scoped>
.upload-container {
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
  flex-direction: column;
  gap: 16px;
  align-items: stretch;
}

.upload-section, .files-section {
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

.empty {
  text-align: center;
  color: var(--color-foreground-secondary);
  padding: 12px;
  font-style: italic;
  font-size: 10px;
}

/* 파일 업로드/다운로드 관련 스타일 */
.upload-area {
  border: 2px dashed var(--color-border);
  border-radius: 8px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
}

.upload-area:hover, .upload-area.drag-over {
  border-color: #6b7280;
  background: var(--color-background);
}

.upload-content {
  pointer-events: none;
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-foreground);
  margin: 0 0 4px 0;
}

.upload-hint {
  font-size: 10px;
  color: var(--color-foreground-secondary);
  margin: 0;
}

.upload-progress {
  background: var(--color-background);
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 12px;
}

.upload-progress h3 {
  font-size: 12px;
  margin: 0 0 6px 0;
  color: var(--color-foreground);
}

.upload-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid var(--color-border);
}

.upload-item:last-child {
  border-bottom: none;
}

.upload-filename {
  font-weight: 500;
  color: var(--color-foreground);
  font-size: 10px;
}

.upload-status {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.upload-status.uploading {
  background: #fef3c7;
  color: #92400e;
}

.upload-status.success {
  background: #dcfce7;
  color: #166534;
}

.upload-status.error {
  background: #fee2e2;
  color: #dc2626;
}


.files-table {
  background: var(--color-background);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 80px 120px 60px;
  gap: 8px;
  padding: 8px;
  background: var(--color-background-tertiary);
  font-weight: 600;
  font-size: 10px;
  color: var(--color-foreground);
  border-bottom: 1px solid var(--color-border);
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 80px 120px 60px;
  gap: 8px;
  padding: 8px;
  border-bottom: 1px solid var(--color-border);
  align-items: center;
  transition: background-color 0.2s;
}

.table-row:hover {
  background: var(--color-background-secondary);
}

.table-row:last-child {
  border-bottom: none;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-name {
  font-weight: 500;
  color: var(--color-foreground);
  font-size: 10px;
}

.file-extension {
  font-size: 8px;
  color: var(--color-foreground-secondary);
  font-family: monospace;
}

.file-size {
  font-size: 10px;
  color: var(--color-foreground-secondary);
  text-align: right;
}

.file-date {
  font-size: 9px;
  color: var(--color-foreground-secondary);
}

.file-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  background: none;
  border: none;
  font-size: 12px;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: var(--color-background-secondary);
  transform: scale(1.1);
}

.action-btn.download:hover {
  background: #dcfce7;
}

.action-btn.delete:hover {
  background: #fee2e2;
}

.refresh-btn {
  padding: 4px 8px;
  background: var(--color-background);
  color: var(--color-foreground-secondary);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 10px;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;
}

.refresh-btn:hover {
  background: var(--color-background-tertiary);
  color: var(--color-foreground);
}

.loading {
  text-align: center;
  color: var(--color-foreground-secondary);
  padding: 12px;
  font-style: italic;
  font-size: 10px;
}

@media (max-width: 768px) {
  .upload-container {
    padding: 16px 12px;
  }

  .title {
    font-size: 16px;
  }

  .main-content {
    flex-direction: column;
    gap: 12px;
  }

  .upload-section, .files-section {
    padding: 12px;
    margin-bottom: 12px;
  }

  .upload-area {
    padding: 16px 12px;
  }

  .upload-icon {
    font-size: 24px;
  }

  .upload-text {
    font-size: 12px;
  }

  .table-header, .table-row {
    grid-template-columns: 1fr;
    gap: 4px;
  }

  .table-header {
    display: none;
  }

  .table-row {
    padding: 8px;
    display: block;
  }

  .file-info {
    margin-bottom: 4px;
  }

  .file-size, .file-date {
    font-size: 9px;
    margin-bottom: 2px;
  }

  .file-actions {
    justify-content: flex-start;
    margin-top: 4px;
  }

  .upload-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
