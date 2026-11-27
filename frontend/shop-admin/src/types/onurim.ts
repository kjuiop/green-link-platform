export type OnurimCompanyType = 'ORDERING' | 'VENDOR' | 'BOTH'
export type OnurimApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface OnurimApplication {
  id: number
  status: OnurimApplicationStatus
  desiredType: OnurimCompanyType
  approvedType: OnurimCompanyType | null
  rejectReason: string | null
  processedAt: string | null
  createdAt: string
}

export interface OnurimRegion {
  sido: string
  sigungu: string
}

export interface OnurimApplyForm {
  desiredType: OnurimCompanyType
  companyPhone: string
  address: string
  detailAddress: string
  taxInvoiceEmail: string
  memo: string
  faxNumber: string
  bankName: string
  accountNumber: string
  accountHolder: string
  regions: OnurimRegion[]
}

export interface OnurimDocumentRequirement {
  needsBusinessReg: boolean
  needsBankBook: boolean
}

export interface OnurimApplyDocuments {
  businessDoc: File | null
  bankDoc: File | null
}
