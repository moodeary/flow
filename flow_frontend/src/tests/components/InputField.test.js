import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InputField from '@/components/common/InputField.vue'

describe('InputField.vue', () => {
  let wrapper

  beforeEach(() => {
    wrapper = mount(InputField)
  })

  afterEach(() => {
    wrapper.unmount()
  })

  describe('기본 렌더링', () => {
    it('기본 구조가 올바르게 렌더링됨', () => {
      expect(wrapper.find('.input-field').exists()).toBe(true)
      expect(wrapper.find('.input-container').exists()).toBe(true)
      expect(wrapper.find('.input-control').exists()).toBe(true)
    })

    it('기본 props가 올바르게 설정됨', () => {
      const input = wrapper.find('.input-control')
      expect(input.attributes('type')).toBe('text')
      expect(input.attributes('placeholder')).toBe('')
      expect(input.element.value).toBe('')
    })

    it('기본 size와 variant 클래스가 적용됨', () => {
      expect(wrapper.find('.input-field-sm').exists()).toBe(true)
      expect(wrapper.find('.input-field-outline').exists()).toBe(true)
    })
  })

  describe('Props 테스트', () => {
    it('label이 올바르게 렌더링됨', async () => {
      await wrapper.setProps({ label: '이름' })

      const label = wrapper.find('.input-label')
      expect(label.exists()).toBe(true)
      expect(label.text()).toBe('이름')
    })

    it('required label이 올바르게 렌더링됨', async () => {
      await wrapper.setProps({ label: '이름', required: true })

      expect(wrapper.find('.input-label-required').exists()).toBe(true)
      expect(wrapper.find('.required-indicator').text()).toBe('*')
    })

    it('placeholder가 올바르게 설정됨', async () => {
      await wrapper.setProps({ placeholder: '이름을 입력하세요' })

      const input = wrapper.find('.input-control')
      expect(input.attributes('placeholder')).toBe('이름을 입력하세요')
    })

    it('modelValue가 올바르게 설정됨', async () => {
      await wrapper.setProps({ modelValue: '테스트 값' })

      const input = wrapper.find('.input-control')
      expect(input.element.value).toBe('테스트 값')
    })

    it('disabled 상태가 올바르게 적용됨', async () => {
      await wrapper.setProps({ disabled: true })

      const input = wrapper.find('.input-control')
      expect(input.attributes('disabled')).toBeDefined()
      expect(wrapper.find('.input-field-disabled').exists()).toBe(true)
    })

    it('readonly 상태가 올바르게 적용됨', async () => {
      await wrapper.setProps({ readonly: true })

      const input = wrapper.find('.input-control')
      expect(input.attributes('readonly')).toBeDefined()
      expect(wrapper.find('.input-field-readonly').exists()).toBe(true)
    })

    it('maxlength가 올바르게 설정됨', async () => {
      await wrapper.setProps({ maxlength: 10 })

      const input = wrapper.find('.input-control')
      expect(input.attributes('maxlength')).toBe('10')
    })
  })

  describe('Size variants', () => {
    it('sm size가 올바르게 적용됨', async () => {
      await wrapper.setProps({ size: 'sm' })
      expect(wrapper.find('.input-field-sm').exists()).toBe(true)
    })

    it('md size가 올바르게 적용됨', async () => {
      await wrapper.setProps({ size: 'md' })
      expect(wrapper.find('.input-field-md').exists()).toBe(true)
    })

    it('lg size가 올바르게 적용됨', async () => {
      await wrapper.setProps({ size: 'lg' })
      expect(wrapper.find('.input-field-lg').exists()).toBe(true)
    })
  })

  describe('Type variants', () => {
    it('password 타입이 올바르게 설정됨', async () => {
      await wrapper.setProps({ type: 'password' })

      const input = wrapper.find('.input-control')
      expect(input.attributes('type')).toBe('password')
      expect(wrapper.find('.input-password-toggle').exists()).toBe(true)
    })

    it('email 타입이 올바르게 설정됨', async () => {
      await wrapper.setProps({ type: 'email' })

      const input = wrapper.find('.input-control')
      expect(input.attributes('type')).toBe('email')
    })

    it('textarea 타입이 올바르게 렌더링됨', async () => {
      await wrapper.setProps({ type: 'textarea' })

      expect(wrapper.find('textarea.input-control').exists()).toBe(true)
    })
  })

  describe('Error 상태', () => {
    it('에러 메시지가 올바르게 표시됨', async () => {
      await wrapper.setProps({ errorMessage: '필수 입력 항목입니다' })

      expect(wrapper.find('.input-field-error').exists()).toBe(true)
      expect(wrapper.find('.input-error').exists()).toBe(true)
      expect(wrapper.find('.input-error').text()).toBe('필수 입력 항목입니다')
    })

    it('hint가 올바르게 표시됨', async () => {
      await wrapper.setProps({ hint: '도움말 텍스트' })

      expect(wrapper.find('.input-hint').exists()).toBe(true)
      expect(wrapper.find('.input-hint').text()).toBe('도움말 텍스트')
    })

    it('에러가 있을 때 hint가 표시되지 않음', async () => {
      await wrapper.setProps({
        errorMessage: '에러 메시지',
        hint: '도움말 텍스트'
      })

      expect(wrapper.find('.input-error').exists()).toBe(true)
      expect(wrapper.find('.input-hint').exists()).toBe(false)
    })
  })

  describe('이벤트 처리', () => {
    it('input 이벤트가 올바르게 발생함', async () => {
      const input = wrapper.find('.input-control')
      await input.setValue('새로운 값')

      const emittedEvents = wrapper.emitted('update:modelValue')
      expect(emittedEvents).toHaveLength(1)
      expect(emittedEvents[0][0]).toBe('새로운 값')
    })

    it('focus 이벤트가 올바르게 발생함', async () => {
      const input = wrapper.find('.input-control')
      await input.trigger('focus')

      expect(wrapper.find('.input-field-focused').exists()).toBe(true)
      expect(wrapper.emitted('focus')).toHaveLength(1)
    })

    it('blur 이벤트가 올바르게 발생함', async () => {
      const input = wrapper.find('.input-control')
      await input.trigger('focus')
      await input.trigger('blur')

      expect(wrapper.find('.input-field-focused').exists()).toBe(false)
      expect(wrapper.emitted('blur')).toHaveLength(1)
    })

    it('Enter 키 이벤트가 올바르게 발생함', async () => {
      const input = wrapper.find('.input-control')
      await input.trigger('keydown', { key: 'Enter' })

      expect(wrapper.emitted('enter')).toHaveLength(1)
    })
  })

  describe('Clearable 기능', () => {
    beforeEach(async () => {
      await wrapper.setProps({
        clearable: true,
        modelValue: '테스트 값'
      })
    })

    it('clear 버튼이 올바르게 표시됨', () => {
      expect(wrapper.find('.input-clear-btn').exists()).toBe(true)
    })

    it('값이 없을 때 clear 버튼이 표시되지 않음', async () => {
      await wrapper.setProps({ modelValue: '' })
      expect(wrapper.find('.input-clear-btn').exists()).toBe(false)
    })

    it('disabled 상태에서 clear 버튼이 표시되지 않음', async () => {
      await wrapper.setProps({ disabled: true })
      expect(wrapper.find('.input-clear-btn').exists()).toBe(false)
    })

    it('clear 버튼 클릭 시 값이 초기화됨', async () => {
      const clearBtn = wrapper.find('.input-clear-btn')
      await clearBtn.trigger('click')

      const emittedUpdate = wrapper.emitted('update:modelValue')
      const emittedClear = wrapper.emitted('clear')

      expect(emittedUpdate[emittedUpdate.length - 1][0]).toBe('')
      expect(emittedClear).toHaveLength(1)
    })
  })

  describe('Password 토글 기능', () => {
    beforeEach(async () => {
      await wrapper.setProps({ type: 'password' })
    })

    it('password 토글 버튼이 표시됨', () => {
      expect(wrapper.find('.input-password-toggle').exists()).toBe(true)
    })

    it('초기 상태에서 비밀번호가 숨겨짐', () => {
      const input = wrapper.find('.input-control')
      expect(input.attributes('type')).toBe('password')
    })

    it('토글 버튼 클릭 시 비밀번호가 표시됨', async () => {
      const toggleBtn = wrapper.find('.input-password-toggle')
      await toggleBtn.trigger('click')

      const input = wrapper.find('.input-control')
      expect(input.attributes('type')).toBe('text')
    })

    it('두 번 클릭 시 다시 비밀번호가 숨겨짐', async () => {
      const toggleBtn = wrapper.find('.input-password-toggle')
      await toggleBtn.trigger('click')
      await toggleBtn.trigger('click')

      const input = wrapper.find('.input-control')
      expect(input.attributes('type')).toBe('password')
    })
  })

  describe('Counter 기능', () => {
    it('showCounter가 true일 때 카운터가 표시됨', async () => {
      await wrapper.setProps({
        showCounter: true,
        maxlength: 10,
        modelValue: 'test'
      })

      const counter = wrapper.find('.input-counter')
      expect(counter.exists()).toBe(true)
      expect(counter.text()).toBe('4/10')
    })

    it('showCounter가 false일 때 카운터가 표시되지 않음', async () => {
      await wrapper.setProps({
        showCounter: false,
        maxlength: 10,
        modelValue: 'test'
      })

      expect(wrapper.find('.input-counter').exists()).toBe(false)
    })

    it('카운터가 실시간으로 업데이트됨', async () => {
      await wrapper.setProps({
        showCounter: true,
        maxlength: 10,
        modelValue: 'hello'
      })

      const counter = wrapper.find('.input-counter')
      expect(counter.text()).toBe('5/10')
    })
  })

  describe('Slots', () => {
    it('prepend slot이 올바르게 렌더링됨', () => {
      const wrapperWithSlot = mount(InputField, {
        slots: {
          prepend: '<span class="prepend-content">@</span>'
        }
      })

      expect(wrapperWithSlot.find('.input-prepend').exists()).toBe(true)
      expect(wrapperWithSlot.find('.prepend-content').text()).toBe('@')

      wrapperWithSlot.unmount()
    })

    it('append slot이 올바르게 렌더링됨', () => {
      const wrapperWithSlot = mount(InputField, {
        slots: {
          append: '<span class="append-content">.com</span>'
        }
      })

      expect(wrapperWithSlot.find('.input-append').exists()).toBe(true)
      expect(wrapperWithSlot.find('.append-content').text()).toBe('.com')

      wrapperWithSlot.unmount()
    })
  })

  describe('Exposed methods', () => {
    it('focus 메서드가 올바르게 동작함', async () => {
      const focusSpy = vi.spyOn(wrapper.vm.inputRef, 'focus')

      wrapper.vm.focus()

      expect(focusSpy).toHaveBeenCalled()
    })

    it('blur 메서드가 올바르게 동작함', async () => {
      const blurSpy = vi.spyOn(wrapper.vm.inputRef, 'blur')

      wrapper.vm.blur()

      expect(blurSpy).toHaveBeenCalled()
    })
  })

  describe('Accessibility', () => {
    it('aria-describedby가 올바르게 설정됨', async () => {
      await wrapper.setProps({ errorMessage: '에러 메시지' })

      const input = wrapper.find('.input-control')
      const description = wrapper.find('.input-description')

      expect(input.attributes('aria-describedby')).toBe(description.attributes('id'))
    })

    it('aria-invalid가 에러 상태에 따라 설정됨', async () => {
      const input = wrapper.find('.input-control')
      expect(input.attributes('aria-invalid')).toBe('false')

      await wrapper.setProps({ errorMessage: '에러 메시지' })
      expect(input.attributes('aria-invalid')).toBe('true')
    })

    it('required 속성이 올바르게 설정됨', async () => {
      await wrapper.setProps({ required: true })

      const input = wrapper.find('.input-control')
      expect(input.attributes('required')).toBeDefined()
    })
  })
})