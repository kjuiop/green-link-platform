import { OrderPhoto } from '@/types/photo'

export const mockPhotos: OrderPhoto[] = [
  {
    id: 'p1',
    orderId: '3',
    url: 'https://placehold.co/400x300/4ade80/ffffff?text=Store+Photo',
    photoType: 'store',
    isSendToCustomer: true,
    uploadedAt: '2026-10-15 10:30',
  },
  {
    id: 'p2',
    orderId: '3',
    url: 'https://placehold.co/400x300/86efac/ffffff?text=Store+Ribbon',
    photoType: 'store',
    isSendToCustomer: false,
    uploadedAt: '2026-10-15 10:31',
  },
  {
    id: 'p3',
    orderId: '3',
    url: 'https://placehold.co/400x300/34d399/ffffff?text=Site+Photo',
    photoType: 'site',
    isSendToCustomer: true,
    uploadedAt: '2026-10-15 10:32',
  },
  {
    id: 'p4',
    orderId: '3',
    url: 'https://placehold.co/400x300/6ee7b7/ffffff?text=Site+Wide',
    photoType: 'site',
    isSendToCustomer: false,
    uploadedAt: '2026-10-15 10:33',
  },
]
