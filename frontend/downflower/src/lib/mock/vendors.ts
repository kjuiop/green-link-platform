import { Vendor } from '@/types/vendor'

export const mockVendors: Vendor[] = [
  {
    id: 'v1',
    name: '서울꽃배달',
    managerName: '김담당',
    phone: '010-1111-1111',
    address: '서울특별시 강남구',
    loginId: 'seoul_flower',
    status: 'active',
    memo: '메인 거래 업체',
    createdAt: '2026-01-01',
  },
  {
    id: 'v2',
    name: '인천꽃집',
    managerName: '이담당',
    phone: '010-2222-2222',
    address: '인천광역시 연수구',
    loginId: 'incheon_flower',
    status: 'active',
    createdAt: '2026-02-01',
  },
  {
    id: 'v3',
    name: '부산화원',
    managerName: '박담당',
    phone: '010-3333-3333',
    address: '부산광역시 해운대구',
    loginId: 'busan_flower',
    status: 'suspended',
    createdAt: '2026-03-01',
  },
]
