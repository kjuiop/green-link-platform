import { Vendor } from '@/types/vendor'

export const mockVendors: Vendor[] = [
  {
    id: 'v1',
    name: '서울꽃배달',
    businessNumber: '123-45-67890',
    managerName: '김담당',
    phone: '010-1111-1111',
    address: '서울특별시 강남구',
    loginId: 'seoul_flower',
    status: 'ACTIVE',
    createdAt: '2026-01-01',
  },
  {
    id: 'v2',
    name: '인천꽃집',
    businessNumber: '234-56-78901',
    managerName: '이담당',
    phone: '010-2222-2222',
    address: '인천광역시 연수구',
    loginId: 'incheon_flower',
    status: 'ACTIVE',
    createdAt: '2026-02-01',
  },
  {
    id: 'v3',
    name: '부산화원',
    businessNumber: '345-67-89012',
    managerName: '박담당',
    phone: '010-3333-3333',
    address: '부산광역시 해운대구',
    loginId: 'busan_flower',
    status: 'INACTIVE',
    createdAt: '2026-03-01',
  },
]
