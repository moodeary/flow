<template>
  <button
    type="button"
    class="toggle-button"
    :class="toggleClasses"
    :aria-pressed="modelValue"
    :aria-labelledby="labelId"
    :aria-describedby="descriptionId"
    :disabled="disabled"
    @click="handleToggle"
  >
    <span class="toggle-track">
      <span class="toggle-thumb">
        <Transition name="icon" mode="out-in">
          <component
            :is="currentIcon"
            v-if="currentIcon"
            class="toggle-icon"
          />
        </Transition>
      </span>
    </span>

    <span v-if="label || $slots.label" :id="labelId" class="toggle-label">
      <slot name="label">{{ label }}</slot>
    </span>

    <span
      v-if="description || $slots.description"
      :id="descriptionId"
      class="toggle-description"
    >
      <slot name="description">{{ description }}</slot>
    </span>
  </button>
</template>

<script setup>
import { computed, ref } from 'vue'

// Simple icon components
const CheckIcon = {
  template: `
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <path d="M10 3L4.5 8.5L2 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `
}

const XIcon = {
  template: `
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <path d="M9 3L3 9M3 3l6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  `
}

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  label: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'sm',
    validator: (value) => ['sm', 'md', 'lg'].includes(value)
  },
  color: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'success', 'warning', 'error'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showIcons: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const labelId = ref(`toggle-label-${Math.random().toString(36).substr(2, 9)}`)
const descriptionId = ref(`toggle-desc-${Math.random().toString(36).substr(2, 9)}`)

const toggleClasses = computed(() => [
  `toggle-${props.size}`,
  `toggle-${props.color}`,
  {
    'toggle-on': props.modelValue,
    'toggle-disabled': props.disabled,
    'toggle-loading': props.loading,
    'toggle-with-content': props.label || props.description
  }
])

const currentIcon = computed(() => {
  if (!props.showIcons || props.loading) return null
  return props.modelValue ? CheckIcon : XIcon
})

const handleToggle = () => {
  if (props.disabled || props.loading) return

  const newValue = !props.modelValue
  emit('update:modelValue', newValue)
  emit('change', newValue)
}
</script>

<style scoped>
.toggle-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: opacity var(--transition-fast);
  text-align: left;
}

.toggle-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toggle-button:focus-visible {
  outline: 2px solid var(--color-accent);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

.toggle-track {
  position: relative;
  border-radius: var(--radius-full);
  transition: all var(--transition-base);
  flex-shrink: 0;
  border: 1px solid var(--color-border);
  background-color: var(--color-background-tertiary);
}

.toggle-thumb {
  position: relative;
  border-radius: 50%;
  background-color: white;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border-secondary);
}

.toggle-icon {
  color: var(--color-foreground-secondary);
}

/* Size variants */
.toggle-sm .toggle-track {
  width: 28px;
  height: 16px;
  padding: 1px;
}

.toggle-sm .toggle-thumb {
  width: 12px;
  height: 12px;
  transform: translateX(0);
}

.toggle-sm.toggle-on .toggle-thumb {
  transform: translateX(12px);
}

.toggle-md .toggle-track {
  width: 44px;
  height: 24px;
  padding: 2px;
}

.toggle-md .toggle-thumb {
  width: 18px;
  height: 18px;
  transform: translateX(0);
}

.toggle-md.toggle-on .toggle-thumb {
  transform: translateX(20px);
}

.toggle-lg .toggle-track {
  width: 52px;
  height: 28px;
  padding: 2px;
}

.toggle-lg .toggle-thumb {
  width: 22px;
  height: 22px;
  transform: translateX(0);
}

.toggle-lg.toggle-on .toggle-thumb {
  transform: translateX(24px);
}

/* Color variants - ON state */
.toggle-on.toggle-primary .toggle-track {
  background-color: var(--color-accent);
  border-color: var(--color-accent);
}

.toggle-on.toggle-primary .toggle-thumb {
  background-color: white;
  border-color: var(--color-accent);
}

.toggle-on.toggle-primary .toggle-icon {
  color: var(--color-accent);
}

.toggle-on.toggle-success .toggle-track {
  background-color: var(--color-success);
  border-color: var(--color-success);
}

.toggle-on.toggle-success .toggle-thumb {
  background-color: white;
  border-color: var(--color-success);
}

.toggle-on.toggle-success .toggle-icon {
  color: var(--color-success);
}

.toggle-on.toggle-warning .toggle-track {
  background-color: var(--color-warning);
  border-color: var(--color-warning);
}

.toggle-on.toggle-warning .toggle-thumb {
  background-color: white;
  border-color: var(--color-warning);
}

.toggle-on.toggle-warning .toggle-icon {
  color: var(--color-warning);
}

.toggle-on.toggle-error .toggle-track {
  background-color: var(--color-error);
  border-color: var(--color-error);
}

.toggle-on.toggle-error .toggle-thumb {
  background-color: white;
  border-color: var(--color-error);
}

.toggle-on.toggle-error .toggle-icon {
  color: var(--color-error);
}

/* Loading state */
.toggle-loading .toggle-thumb::after {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  border: 1px solid transparent;
  border-top: 1px solid var(--color-foreground-tertiary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Label and description */
.toggle-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-foreground);
  line-height: 1.3;
}

.toggle-description {
  font-size: 10px;
  color: var(--color-foreground-secondary);
  line-height: 1.3;
}

.toggle-with-content {
  flex-direction: column;
  align-items: flex-start;
  gap: var(--spacing-sm);
}

.toggle-with-content .toggle-track {
  order: -1;
  margin-bottom: var(--spacing-xs);
}

@media (min-width: 768px) {
  .toggle-with-content {
    flex-direction: row;
    align-items: center;
    gap: var(--spacing-md);
  }

  .toggle-with-content .toggle-track {
    order: 0;
    margin-bottom: 0;
  }
}

/* Hover effects */
.toggle-button:not(.toggle-disabled):not(.toggle-loading):hover .toggle-track {
  border-color: var(--color-border-secondary);
}

.toggle-on:not(.toggle-disabled):not(.toggle-loading):hover.toggle-primary .toggle-track {
  background-color: var(--color-accent-hover);
  border-color: var(--color-accent-hover);
}

/* Transitions */
.icon-enter-active,
.icon-leave-active {
  transition: all 0.15s ease;
}

.icon-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.icon-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>