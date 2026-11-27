import type { OnurimApplication, OnurimApplyDocuments, OnurimApplyForm, OnurimDocumentRequirement } from '@/types/onurim'
import { apiFetch } from './client'

export function fetchOnurimApplication(): Promise<OnurimApplication | undefined> {
  return apiFetch<OnurimApplication | undefined>('/api/onurim-application')
}

export function fetchOnurimDocumentRequirement(): Promise<OnurimDocumentRequirement> {
  return apiFetch<OnurimDocumentRequirement>('/api/onurim-application/documents')
}

export function applyOnurim(form: OnurimApplyForm, docs: OnurimApplyDocuments): Promise<OnurimApplication> {
  const body = new FormData()
  body.append('data', new Blob([JSON.stringify(form)], { type: 'application/json' }))
  if (docs.businessDoc) body.append('businessDoc', docs.businessDoc, docs.businessDoc.name)
  if (docs.bankDoc) body.append('bankDoc', docs.bankDoc, docs.bankDoc.name)
  return apiFetch<OnurimApplication>('/api/onurim-application', { method: 'POST', body })
}
