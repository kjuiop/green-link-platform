export interface MockOrder {
  id: string
  orderNumber: string
  orderingCompanyName: string
  vendorName: string
  productName: string
  placeName: string
  ordererName: string
  deliveredAt: string
  orderAmount: number
  vendorAmount: number
}

export const MOCK_ORDERS: MockOrder[] = [
  // 다운플라워 → 꽃향기
  { id: '1',  orderNumber: 'DN-20261101-001', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '축하화환 대',  placeName: '강남구청',      ordererName: '김민준', deliveredAt: '2026-11-01', orderAmount: 150000, vendorAmount: 120000 },
  { id: '2',  orderNumber: 'DN-20261103-002', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '근조화환 중',  placeName: '서초 장례식장', ordererName: '이서연', deliveredAt: '2026-11-03', orderAmount: 100000, vendorAmount: 80000  },
  { id: '3',  orderNumber: 'DN-20261105-003', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '동양란',        placeName: '서초 웨딩홀',   ordererName: '박도윤', deliveredAt: '2026-11-05', orderAmount: 80000,  vendorAmount: 65000  },
  { id: '4',  orderNumber: 'DN-20261108-004', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '축하화환 소',  placeName: '마포 사무실',   ordererName: '정시우', deliveredAt: '2026-11-08', orderAmount: 90000,  vendorAmount: 72000  },
  { id: '5',  orderNumber: 'DN-20261112-005', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '쌀화환',        placeName: '강남 웨딩홀',   ordererName: '한지호', deliveredAt: '2026-11-12', orderAmount: 180000, vendorAmount: 144000 },
  { id: '6',  orderNumber: 'DN-20261115-006', orderingCompanyName: '다운플라워', vendorName: '꽃향기',    productName: '근조화환 대',  placeName: '송파 장례식장', ordererName: '오서준', deliveredAt: '2026-11-15', orderAmount: 200000, vendorAmount: 160000 },

  // 다운플라워 → 한강꽃집
  { id: '7',  orderNumber: 'DN-20261102-007', orderingCompanyName: '다운플라워', vendorName: '한강꽃집',  productName: '축하화환 중',  placeName: '여의도 증권사', ordererName: '윤준서', deliveredAt: '2026-11-02', orderAmount: 120000, vendorAmount: 96000  },
  { id: '8',  orderNumber: 'DN-20261106-008', orderingCompanyName: '다운플라워', vendorName: '한강꽃집',  productName: '서양란',        placeName: '마포 사무실',   ordererName: '임채원', deliveredAt: '2026-11-06', orderAmount: 85000,  vendorAmount: 68000  },
  { id: '9',  orderNumber: 'DN-20261110-009', orderingCompanyName: '다운플라워', vendorName: '한강꽃집',  productName: '근조화환 대',  placeName: '용산 장례식장', ordererName: '최지아', deliveredAt: '2026-11-10', orderAmount: 200000, vendorAmount: 160000 },
  { id: '10', orderNumber: 'DN-20261114-010', orderingCompanyName: '다운플라워', vendorName: '한강꽃집',  productName: '축하화환 대',  placeName: '강동 사무실',   ordererName: '신예은', deliveredAt: '2026-11-14', orderAmount: 150000, vendorAmount: 120000 },

  // 다운플라워 → 남산꽃원
  { id: '11', orderNumber: 'DN-20261104-011', orderingCompanyName: '다운플라워', vendorName: '남산꽃원',  productName: '근조화환 중',  placeName: '중구 장례식장', ordererName: '권민서', deliveredAt: '2026-11-04', orderAmount: 110000, vendorAmount: 88000  },
  { id: '12', orderNumber: 'DN-20261109-012', orderingCompanyName: '다운플라워', vendorName: '남산꽃원',  productName: '쌀화환',        placeName: '송파 웨딩홀',   ordererName: '황지민', deliveredAt: '2026-11-09', orderAmount: 130000, vendorAmount: 104000 },

  // 그린플라워 → 한강꽃집
  { id: '13', orderNumber: 'GN-20261101-013', orderingCompanyName: '그린플라워', vendorName: '한강꽃집',  productName: '근조화환 대',  placeName: '부산 장례식장', ordererName: '김서준', deliveredAt: '2026-11-01', orderAmount: 180000, vendorAmount: 144000 },
  { id: '14', orderNumber: 'GN-20261104-014', orderingCompanyName: '그린플라워', vendorName: '한강꽃집',  productName: '축하화환 중',  placeName: '해운대 웨딩홀', ordererName: '이지원', deliveredAt: '2026-11-04', orderAmount: 120000, vendorAmount: 96000  },
  { id: '15', orderNumber: 'GN-20261107-015', orderingCompanyName: '그린플라워', vendorName: '한강꽃집',  productName: '동양란',        placeName: '부산 사무실',   ordererName: '박민아', deliveredAt: '2026-11-07', orderAmount: 75000,  vendorAmount: 60000  },
  { id: '16', orderNumber: 'GN-20261111-016', orderingCompanyName: '그린플라워', vendorName: '한강꽃집',  productName: '서양란',        placeName: '해운대 사무실', ordererName: '최준혁', deliveredAt: '2026-11-11', orderAmount: 85000,  vendorAmount: 68000  },

  // 그린플라워 → 부산꽃향기
  { id: '17', orderNumber: 'GN-20261103-017', orderingCompanyName: '그린플라워', vendorName: '부산꽃향기', productName: '근조화환 중',  placeName: '부산 장례식장', ordererName: '윤세아', deliveredAt: '2026-11-03', orderAmount: 100000, vendorAmount: 80000  },
  { id: '18', orderNumber: 'GN-20261108-018', orderingCompanyName: '그린플라워', vendorName: '부산꽃향기', productName: '쌀화환',        placeName: '부산 웨딩홀',   ordererName: '임도현', deliveredAt: '2026-11-08', orderAmount: 130000, vendorAmount: 104000 },
  { id: '19', orderNumber: 'GN-20261113-019', orderingCompanyName: '그린플라워', vendorName: '부산꽃향기', productName: '축하화환 대',  placeName: '광안리 호텔',   ordererName: '한수빈', deliveredAt: '2026-11-13', orderAmount: 150000, vendorAmount: 120000 },

  // 블룸코리아 → 남산꽃원
  { id: '20', orderNumber: 'BL-20261102-020', orderingCompanyName: '블룸코리아', vendorName: '남산꽃원',  productName: '축하화환 대',  placeName: '강동 사무실',   ordererName: '오지훈', deliveredAt: '2026-11-02', orderAmount: 160000, vendorAmount: 128000 },
  { id: '21', orderNumber: 'BL-20261105-021', orderingCompanyName: '블룸코리아', vendorName: '남산꽃원',  productName: '근조화환 중',  placeName: '성동 장례식장', ordererName: '신하은', deliveredAt: '2026-11-05', orderAmount: 110000, vendorAmount: 88000  },
  { id: '22', orderNumber: 'BL-20261109-022', orderingCompanyName: '블룸코리아', vendorName: '남산꽃원',  productName: '서양란',        placeName: '마포 호텔',     ordererName: '권태양', deliveredAt: '2026-11-09', orderAmount: 85000,  vendorAmount: 68000  },

  // 블룸코리아 → 꽃향기
  { id: '23', orderNumber: 'BL-20261106-023', orderingCompanyName: '블룸코리아', vendorName: '꽃향기',    productName: '쌀화환',        placeName: '강남 호텔',     ordererName: '황도윤', deliveredAt: '2026-11-06', orderAmount: 180000, vendorAmount: 144000 },
  { id: '24', orderNumber: 'BL-20261111-024', orderingCompanyName: '블룸코리아', vendorName: '꽃향기',    productName: '근조화환 대',  placeName: '서초 장례식장', ordererName: '김나연', deliveredAt: '2026-11-11', orderAmount: 200000, vendorAmount: 160000 },
  { id: '25', orderNumber: 'BL-20261115-025', orderingCompanyName: '블룸코리아', vendorName: '꽃향기',    productName: '동양란',        placeName: '강남 사무실',   ordererName: '이준서', deliveredAt: '2026-11-15', orderAmount: 80000,  vendorAmount: 64000  },

  // 에이플라워 → 부산꽃향기
  { id: '26', orderNumber: 'AF-20261103-026', orderingCompanyName: '에이플라워', vendorName: '부산꽃향기', productName: '축하화환 중',  placeName: '수영구 사무실', ordererName: '박서진', deliveredAt: '2026-11-03', orderAmount: 120000, vendorAmount: 96000  },
  { id: '27', orderNumber: 'AF-20261107-027', orderingCompanyName: '에이플라워', vendorName: '부산꽃향기', productName: '근조화환 중',  placeName: '부산 장례식장', ordererName: '최민재', deliveredAt: '2026-11-07', orderAmount: 100000, vendorAmount: 80000  },
  { id: '28', orderNumber: 'AF-20261112-028', orderingCompanyName: '에이플라워', vendorName: '부산꽃향기', productName: '서양란',        placeName: '해운대 호텔',   ordererName: '윤지아', deliveredAt: '2026-11-12', orderAmount: 90000,  vendorAmount: 72000  },

  // 에이플라워 → 남산꽃원
  { id: '29', orderNumber: 'AF-20261104-029', orderingCompanyName: '에이플라워', vendorName: '남산꽃원',  productName: '근조화환 대',  placeName: '중구 장례식장', ordererName: '임성현', deliveredAt: '2026-11-04', orderAmount: 200000, vendorAmount: 160000 },
  { id: '30', orderNumber: 'AF-20261110-030', orderingCompanyName: '에이플라워', vendorName: '남산꽃원',  productName: '쌀화환',        placeName: '동작구 웨딩홀', ordererName: '한예지', deliveredAt: '2026-11-10', orderAmount: 130000, vendorAmount: 104000 },
]

export function formatKRW(amount: number) {
  return amount.toLocaleString('ko-KR') + '원'
}
