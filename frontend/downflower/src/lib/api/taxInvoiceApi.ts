import { authHeaders, fetchWithAuth } from './fetchWithAuth'

export type TaxInvoiceStatus = 'ISSUING' | 'SUCCESS' | 'FAILED'

export interface TaxInvoiceParty {
  corpNum: string | null
  companyName: string | null
  ceoName: string | null
  address: string | null
  email: string | null
}

export interface TaxInvoicePreview {
  pointTransactionId: number
  itemName: string
  supplyAmount: number
  taxAmount: number
  totalAmount: number
  supplier: TaxInvoiceParty
  recipient: TaxInvoiceParty
  status: TaxInvoiceStatus | null
  failReason: string | null
  missing: string[]
}

export interface TaxInvoiceResult {
  pointTransactionId: number
  status: TaxInvoiceStatus
  failReason: string | null
  recipientEmail: string | null
}

async function errorMessage(res: Response, fallback: string): Promise<string> {
  try {
    const body = await res.json()
    return body.message || fallback
  } catch {
    return fallback
  }
}

export async function previewTaxInvoice(transactionId: number): Promise<TaxInvoicePreview> {
  const res = await fetchWithAuth(`/api/admin/points/transactions/${transactionId}/tax-invoice/preview`, {
    headers: authHeaders(),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '계산서 미리보기를 불러오지 못했습니다.'))
  return res.json()
}

export async function issueTaxInvoice(transactionId: number, recipientEmail: string): Promise<TaxInvoiceResult> {
  const res = await fetchWithAuth(`/api/admin/points/transactions/${transactionId}/tax-invoice`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ recipientEmail }),
  })
  if (!res.ok) throw new Error(await errorMessage(res, '계산서 발행에 실패했습니다.'))
  return res.json()
}
