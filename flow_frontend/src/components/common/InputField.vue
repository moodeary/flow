<template>
  <div class="input-field" :class="fieldClasses">
    <label
      v-if="label"
      :for="inputId"
      class="input-label"
      :class="{ 'input-label-required': required }"
    >
      {{ label }}
      <span v-if="required" class="required-indicator" aria-label="필수">*</span>
    </label>

    <div class="input-container">
      <div v-if="$slots.prepend || prependIcon" class="input-prepend">
        <slot name="prepend">
          <component :is="prependIcon" v-if="prependIcon" class="input-icon" />
        </slot>
      </div>

      <component
        :is="inputComponent"
        :id="inputId"
        ref="inputRef"
        :class="inputClasses"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :type="inputType"
        :rows="rows"
        :maxlength="maxlength"
        :aria-describedby="descriptionId"
        :aria-invalid="hasError"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
      />

      <div v-if="$slots.append || appendIcon || clearable || showPasswordToggle" class="input-append">
        <button
          v-if="clearable && modelValue && !disabled && !readonly"
          type="button"
          class="input-clear-btn"
          aria-label="입력 내용 지우기"
          @click="handleClear"
        >
          <ClearIcon class="input-icon" />
        </button>

        <button
          v-if="showPasswordToggle"
          type="button"
          class="input-password-toggle"
          :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
          @click="togglePassword"
        >
          <component
            :is="showPassword ? EyeOffIcon : EyeIcon"
            class="input-icon"
          />
        </button>

        <slot name="append">
          <component :is="appendIcon" v-if="appendIcon" class="input-icon" />
        </slot>
      </div>
    </div>

    <div v-if="hasError || hint" :id="descriptionId" class="input-description">
      <p v-if="hasError" class="input-error">{{ errorMessage }}</p>
      <p v-else-if="hint" class="input-hint">{{ hint }}</p>
    </div>

    <div v-if="showCounter" class="input-counter">
      {{ currentLength }}/{{ maxlength }}
    </div>
  </div>
</template>

<script setup>
import { computed, ref, nextTick } from 'vue'

// Simple icon components
const EyeIcon = {
  template: `
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `
}

const EyeOffIcon = {
  template: `
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      <path d="m1 1 22 22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `
}

const ClearIcon = {
  template: `
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
      <path d="m15 9-6 6M9 9l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `
}

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  },
  errorMessage: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text',
    validator: (value) => ['text', 'password', 'email', 'number', 'tel', 'url', 'search', 'textarea'].includes(value)
  },
  size: {
    type: String,
    default: 'sm',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  variant: {
    type: String,
    default: 'outline',
    validator: (value) => ['outline', 'filled', 'underline'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  clearable: {
    type: Boolean,
    default: false
  },
  rows: {
    type: Number,
    default: 3
  },
  maxlength: {
    type: Number,
    default: null
  },
  showCounter: {
    type: Boolean,
    default: false
  },
  prependIcon: {
    type: Object,
    default: null
  },
  appendIcon: {
    type: Object,
    default: null
  },
  autoFocus: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'focus', 'blur', 'clear', 'enter'])

const inputRef = ref(null)
const isFocused = ref(false)
const showPassword = ref(false)
const inputId = ref(`input-${Math.random().toString(36).substr(2, 9)}`)
const descriptionId = ref(`input-desc-${Math.random().toString(36).substr(2, 9)}`)

const inputComponent = computed(() => props.type === 'textarea' ? 'textarea' : 'input')
const inputType = computed(() => {
  if (props.type === 'password') {
    return showPassword.value ? 'text' : 'password'
  }
  return props.type === 'textarea' ? undefined : props.type
})

const showPasswordToggle = computed(() => props.type === 'password')
const hasError = computed(() => !!props.errorMessage)
const currentLength = computed(() => String(props.modelValue || '').length)

const fieldClasses = computed(() => [
  `input-field-${props.size}`,
  `input-field-${props.variant}`,
  {
    'input-field-focused': isFocused.value,
    'input-field-error': hasError.value,
    'input-field-disabled': props.disabled,
    'input-field-readonly': props.readonly
  }
])

const inputClasses = computed(() => [
  'input-control',
  {
    'input-with-prepend': props.prependIcon || props.$slots?.prepend,
    'input-with-append': props.appendIcon || props.$slots?.append || props.clearable || showPasswordToggle.value
  }
])

const handleInput = (event) => {
  emit('update:modelValue', event.target.value)
}

const handleFocus = (event) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event) => {
  isFocused.value = false
  emit('blur', event)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const handleKeydown = (event) => {
  if (event.key === 'Enter') {
    emit('enter', event)
  }
}

const focus = () => {
  inputRef.value?.focus()
}

const blur = () => {
  inputRef.value?.blur()
}

defineExpose({
  focus,
  blur,
  inputRef
})
</script>

<style scoped>
.input-field {
  width: 100%;
}

.input-label {
  display: block;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-foreground);
  margin-bottom: var(--spacing-xs);
  line-height: 1.4;
}

.input-label-required {
  color: var(--color-foreground);
}

.required-indicator {
  color: var(--color-error);
  margin-left: 2px;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
}

.input-control {
  width: 100%;
  font-family: var(--font-family-system);
  font-weight: var(--font-weight-regular);
  background-color: var(--color-background-secondary);
  color: var(--color-foreground);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  resize: vertical;
}

.input-control:focus {
  outline: none;
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.1);
}

.input-control::placeholder {
  color: var(--color-foreground-tertiary);
}

.input-control:disabled {
  background-color: var(--color-background-tertiary);
  color: var(--color-foreground-tertiary);
  cursor: not-allowed;
  opacity: 0.6;
}

.input-control:readonly {
  background-color: var(--color-background-tertiary);
  cursor: default;
}

/* Size variants */
.input-field-sm .input-control {
  padding: 6px 10px;
  font-size: 12px;
  min-height: 28px;
}

.input-field-md .input-control {
  padding: var(--spacing-md) var(--spacing-md);
  font-size: var(--font-size-base);
  min-height: 44px;
}

.input-field-lg .input-control {
  padding: var(--spacing-lg) var(--spacing-md);
  font-size: var(--font-size-lg);
  min-height: 52px;
}

/* Variant styles */
.input-field-filled .input-control {
  border: none;
  background-color: var(--color-background-tertiary);
}

.input-field-underline .input-control {
  border: none;
  border-bottom: 2px solid var(--color-border);
  border-radius: 0;
  background-color: transparent;
}

.input-field-underline .input-control:focus {
  border-bottom-color: var(--color-accent);
  box-shadow: none;
}

/* Error state */
.input-field-error .input-control {
  border-color: var(--color-error);
}

.input-field-error .input-control:focus {
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px rgba(255, 69, 58, 0.1);
}

/* Prepend/Append */
.input-prepend,
.input-append {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  z-index: 1;
}

.input-prepend {
  left: var(--spacing-md);
}

.input-append {
  right: var(--spacing-md);
}

.input-with-prepend {
  padding-left: 48px;
}

.input-with-append {
  padding-right: 48px;
}

.input-icon {
  width: 20px;
  height: 20px;
  color: var(--color-foreground-tertiary);
  flex-shrink: 0;
}

.input-clear-btn,
.input-password-toggle {
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  color: var(--color-foreground-tertiary);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-clear-btn:hover,
.input-password-toggle:hover {
  background-color: var(--color-background-tertiary);
  color: var(--color-foreground-secondary);
}

/* Description */
.input-description {
  margin-top: var(--spacing-xs);
}

.input-error,
.input-hint {
  font-size: var(--font-size-xs);
  line-height: 1.4;
  margin: 0;
}

.input-error {
  color: var(--color-error);
}

.input-hint {
  color: var(--color-foreground-tertiary);
}

/* Counter */
.input-counter {
  text-align: right;
  font-size: var(--font-size-xs);
  color: var(--color-foreground-tertiary);
  margin-top: var(--spacing-xs);
}

/* Textarea specific */
textarea.input-control {
  resize: vertical;
  min-height: 80px;
  line-height: 1.5;
}

.input-field-sm textarea.input-control {
  min-height: 64px;
}

.input-field-lg textarea.input-control {
  min-height: 96px;
}
</style>