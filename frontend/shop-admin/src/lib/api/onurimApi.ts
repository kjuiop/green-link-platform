import type { OnurimApplication, OnurimApplyForm } from '@/types/onurim'
import { apiFetch } from './client'

export function fetchOnurimApplication(): Promise<OnurimApplication | undefined> {
  return apiFetch<OnurimApplication | undefined>('/api/onurim-application')
}

export function applyOnurim(form: OnurimApplyForm): Promise<OnurimApplication> {
  return apiFetch<OnurimApplication>('/api/onurim-application', { method: 'POST', body: form })
}
