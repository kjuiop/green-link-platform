'use client'

import { Button } from '@/components/ui/button'
import { Printer } from 'lucide-react'

interface Props {
  orderId: string
}

export function PrintButton({ orderId }: Props) {
  const handlePrint = () => {
    const url = `/orders/${orderId}/print`
    window.open(url, '_blank', 'width=900,height=1050,resizable=yes,scrollbars=yes')
  }

  return (
    <Button variant="outline" className="gap-1.5" onClick={handlePrint}>
      <Printer className="h-4 w-4" />
      주문서 인쇄
    </Button>
  )
}
