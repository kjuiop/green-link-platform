'use client'

import { useParams, useRouter } from 'next/navigation'
import { OrderDetailPopup } from '@/components/common/OrderDetailPopup'

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()

  return <OrderDetailPopup orderId={id} variant="page" onClose={() => router.push('/orders')} />
}
