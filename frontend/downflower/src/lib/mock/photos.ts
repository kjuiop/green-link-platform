import { OrderPhoto } from '@/types/photo'

export const mockPhotos: OrderPhoto[] = [
  {
    id: 'p1',
    orderId: '3',
    url: 'https://placehold.co/400x300?text=화환+전체',
    isSendToCustomer: true,
    uploadedAt: '2026-10-15 10:30',
  },
  {
    id: 'p2',
    orderId: '3',
    url: 'https://placehold.co/400x300?text=리본문구+확대',
    isSendToCustomer: false,
    uploadedAt: '2026-10-15 10:31',
  },
  {
    id: 'p3',
    orderId: '3',
    url: 'https://placehold.co/400x300?text=배송+현장',
    isSendToCustomer: false,
    uploadedAt: '2026-10-15 10:32',
  },
]
