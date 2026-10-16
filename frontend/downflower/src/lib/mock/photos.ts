import { OrderPhoto } from '@/types/photo'

export const mockPhotos: OrderPhoto[] = [
  {
    id: 'p1',
    orderId: '3',
    url: 'https://placehold.co/400x300/4ade80/ffffff?text=Flower+Full',
    isSendToCustomer: true,
    uploadedAt: '2026-10-15 10:30',
  },
  {
    id: 'p2',
    orderId: '3',
    url: 'https://placehold.co/400x300/86efac/ffffff?text=Ribbon+Close',
    isSendToCustomer: false,
    uploadedAt: '2026-10-15 10:31',
  },
  {
    id: 'p3',
    orderId: '3',
    url: 'https://placehold.co/400x300/bbf7d0/ffffff?text=Delivery+Site',
    isSendToCustomer: false,
    uploadedAt: '2026-10-15 10:32',
  },
]
