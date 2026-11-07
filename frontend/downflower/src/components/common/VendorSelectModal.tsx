'use client'

import { useState, useMemo } from 'react'
import { ComposableMap, Geographies, Geography } from 'react-simple-maps'
import { X } from 'lucide-react'
import { Vendor } from '@/types/vendor'

const GEO_URL = '/korea-sido.json'

const CAPABILITIES = [
  { value: 'ALL', label: '전체' },
  { value: 'ANNIVERSARY', label: '축하' },
  { value: 'MEMORIAL', label: '근조' },
  { value: 'ORCHID_EAST', label: '동양란' },
  { value: 'ORCHID_WEST', label: '서양란' },
  { value: 'FLOWER', label: '꽃' },
  { value: 'INDOOR', label: '관엽' },
  { value: 'RICE_WREATH', label: '쌀화환' },
]

const SIDO_LIST = [
  '서울특별시', '부산광역시', '대구광역시', '인천광역시', '광주광역시',
  '대전광역시', '울산광역시', '세종특별자치시', '경기도', '강원도',
  '충청북도', '충청남도', '전라북도', '전라남도', '경상북도', '경상남도', '제주특별자치도',
]

const CAP_LABEL: Record<string, string> = {
  ANNIVERSARY: '축하', MEMORIAL: '근조', ORCHID_EAST: '동양란',
  ORCHID_WEST: '서양란', FLOWER: '꽃', INDOOR: '관엽', RICE_WREATH: '쌀화환',
}

const SIGUNGU_BY_SIDO: Record<string, string[]> = {
  '서울특별시': [
    '강남구', '강동구', '강북구', '강서구', '관악구', '광진구', '구로구', '금천구',
    '노원구', '도봉구', '동대문구', '동작구', '마포구', '서대문구', '서초구', '성동구',
    '성북구', '송파구', '양천구', '영등포구', '용산구', '은평구', '종로구', '중구', '중랑구',
  ],
  '부산광역시': [
    '강서구', '금정구', '기장군', '남구', '동구', '동래구', '부산진구', '북구',
    '사상구', '사하구', '서구', '수영구', '연제구', '영도구', '중구', '해운대구',
  ],
  '대구광역시': [
    '군위군', '남구', '달성군', '달서구', '동구', '북구', '서구', '수성구', '중구',
  ],
  '인천광역시': [
    '강화군', '계양구', '남동구', '동구', '미추홀구', '부평구', '서구', '연수구', '옹진군', '중구',
  ],
  '광주광역시': ['광산구', '남구', '동구', '북구', '서구'],
  '대전광역시': ['대덕구', '동구', '서구', '유성구', '중구'],
  '울산광역시': ['남구', '동구', '북구', '울주군', '중구'],
  '세종특별자치시': [],
  '경기도': [
    '가평군', '고양시 덕양구', '고양시 일산동구', '고양시 일산서구', '과천시', '광명시',
    '광주시', '구리시', '군포시', '김포시', '남양주시', '동두천시', '부천시',
    '성남시 분당구', '성남시 수정구', '성남시 중원구',
    '수원시 권선구', '수원시 영통구', '수원시 장안구', '수원시 팔달구',
    '시흥시', '안산시 단원구', '안산시 상록구', '안성시',
    '안양시 동안구', '안양시 만안구', '양주시', '양평군', '여주시', '연천군',
    '오산시', '용인시 기흥구', '용인시 수지구', '용인시 처인구',
    '의왕시', '의정부시', '이천시', '파주시', '평택시', '포천시', '하남시', '화성시',
  ],
  '강원도': [
    '강릉시', '고성군', '동해시', '삼척시', '속초시', '양구군', '양양군', '영월군',
    '원주시', '인제군', '정선군', '철원군', '춘천시', '태백시', '평창군', '홍천군',
    '화천군', '횡성군',
  ],
  '충청북도': [
    '괴산군', '단양군', '보은군', '영동군', '옥천군', '음성군', '제천시', '증평군',
    '진천군', '청주시 상당구', '청주시 서원구', '청주시 청원구', '청주시 흥덕구', '충주시',
  ],
  '충청남도': [
    '계룡시', '공주시', '금산군', '논산시', '당진시', '보령시', '부여군', '서산시',
    '서천군', '아산시', '예산군', '천안시 동남구', '천안시 서북구', '청양군', '태안군', '홍성군',
  ],
  '전라북도': [
    '고창군', '군산시', '김제시', '남원시', '무주군', '부안군', '순창군', '완주군',
    '익산시', '임실군', '장수군', '전주시 덕진구', '전주시 완산구', '정읍시', '진안군',
  ],
  '전라남도': [
    '강진군', '고흥군', '곡성군', '광양시', '구례군', '나주시', '담양군', '목포시',
    '무안군', '보성군', '순천시', '신안군', '여수시', '영광군', '영암군', '완도군',
    '장성군', '장흥군', '진도군', '함평군', '해남군', '화순군',
  ],
  '경상북도': [
    '경산시', '경주시', '고령군', '구미시', '김천시', '문경시', '봉화군', '상주시',
    '성주군', '안동시', '영덕군', '영양군', '영주시', '영천시', '예천군', '울릉군',
    '울진군', '의성군', '청도군', '청송군', '칠곡군', '포항시 남구', '포항시 북구',
  ],
  '경상남도': [
    '거제시', '거창군', '고성군', '김해시', '남해군', '밀양시', '사천시', '산청군',
    '양산시', '의령군', '진주시', '창녕군',
    '창원시 마산합포구', '창원시 마산회원구', '창원시 성산구', '창원시 의창구', '창원시 진해구',
    '통영시', '하동군', '함안군', '함양군', '합천군',
  ],
  '제주특별자치도': ['서귀포시', '제주시'],
}

type Props = {
  open: boolean
  vendors: Vendor[]
  onSelect: (vendor: Vendor) => void
  onClose: () => void
}

export default function VendorSelectModal({ open, vendors, onSelect, onClose }: Props) {
  const [selectedSido, setSelectedSido] = useState<string | null>(null)
  const [selectedSigungu, setSelectedSigungu] = useState<string | null>(null)
  const [selectedCapability, setSelectedCapability] = useState('ALL')
  const [hoveredSido, setHoveredSido] = useState<string | null>(null)

  const sigunguList = useMemo(() => {
    if (!selectedSido) return []
    return SIGUNGU_BY_SIDO[selectedSido] ?? []
  }, [selectedSido])

  const filteredVendors = useMemo(() => {
    return vendors.filter((v) => {
      if (v.status !== 'ACTIVE') return false
      const regionMatch = selectedSido
        ? (v.regions ?? []).some(
            (r) => r.sido === selectedSido && (!selectedSigungu || r.sigungu === selectedSigungu)
          )
        : true
      const capMatch =
        selectedCapability === 'ALL' || (v.capabilities ?? []).includes(selectedCapability)
      return regionMatch && capMatch
    })
  }, [vendors, selectedSido, selectedSigungu, selectedCapability])

  function handleSidoSelect(sido: string) {
    setSelectedSido(sido === selectedSido ? null : sido)
    setSelectedSigungu(null)
  }

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={onClose}>
      <div className="flex w-full max-w-7xl flex-col rounded-lg bg-white shadow-xl" style={{ maxHeight: '95vh' }} onClick={(e) => e.stopPropagation()}>
        {/* 헤더 */}
        <div className="flex items-center justify-between border-b px-4 py-3">
          <h2 className="text-base font-semibold">수주업체 선택</h2>
          <button onClick={onClose} className="rounded p-1 hover:bg-gray-100">
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* 카테고리 탭 */}
        <div className="flex flex-wrap gap-1.5 border-b px-4 py-2">
          {CAPABILITIES.map(({ value, label }) => (
            <button
              key={value}
              onClick={() => setSelectedCapability(value)}
              className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                selectedCapability === value
                  ? 'bg-green-700 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        {/* 본문 */}
        <div className="flex min-h-0 flex-1 overflow-hidden" style={{ minHeight: '700px' }}>
          {/* 지도 영역 */}
          <div className="flex flex-col border-r overflow-y-auto" style={{ width: '45%' }}>
            {/* PC: SVG 지도 */}
            <div className="hidden sm:flex flex-col overflow-hidden" style={{ height: '620px', flexShrink: 0 }}>
              <div className="flex-1 min-h-0 overflow-hidden">
                <ComposableMap
                  projection="geoMercator"
                  projectionConfig={{ center: [127.5, 36], scale: 3800 }}
                  width={430}
                  height={444}
                  style={{ width: '100%', height: '100%' }}
                >
                  <Geographies geography={GEO_URL}>
                    {({ geographies }) =>
                      // eslint-disable-next-line @typescript-eslint/no-explicit-any
                      geographies.map((geo: any) => {
                        const sido = (geo.properties as { name?: string })?.name ?? ''
                        const isSelected = selectedSido === sido
                        return (
                          <Geography
                            key={geo.rsmKey}
                            geography={geo}
                            onClick={() => handleSidoSelect(sido)}
                            fill={isSelected ? '#15803d' : '#d1fae5'}
                            stroke="#fff"
                            strokeWidth={0.5}
                            style={{ cursor: 'pointer', outline: 'none' }}
                            onMouseEnter={(e) => {
                              if (!isSelected) (e.target as SVGPathElement).setAttribute('fill', '#6ee7b7')
                              setHoveredSido(sido)
                            }}
                            onMouseLeave={(e) => {
                              if (!isSelected) (e.target as SVGPathElement).setAttribute('fill', '#d1fae5')
                              setHoveredSido(null)
                            }}
                          />
                        )
                      })
                    }
                  </Geographies>
                </ComposableMap>
              </div>
              <div className="border-t bg-gray-50 px-3 py-1 text-center text-xs text-gray-500" style={{ minHeight: '24px' }}>
                {hoveredSido ?? selectedSido ?? '시도를 선택하세요'}
              </div>
            </div>

            {/* 모바일: 시도 버튼 그리드 */}
            <div className="grid grid-cols-2 gap-1.5 overflow-auto p-3 sm:hidden">
              {SIDO_LIST.map((sido) => (
                <button
                  key={sido}
                  onClick={() => handleSidoSelect(sido)}
                  className={`rounded px-2 py-1.5 text-xs font-medium transition-colors ${
                    selectedSido === sido
                      ? 'bg-green-700 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}
                >
                  {sido}
                </button>
              ))}
            </div>

          </div>

          {/* 업체 목록 */}
          <div className="flex flex-1 flex-col overflow-hidden">
            <div className="border-b px-4 py-2">
              {selectedSido && sigunguList.length > 0 ? (
                <div className="flex items-center gap-2">
                  <span className="shrink-0 text-xs font-medium text-gray-600">{selectedSido}</span>
                  <select
                    value={selectedSigungu ?? ''}
                    onChange={(e) => setSelectedSigungu(e.target.value || null)}
                    className="flex-1 rounded border border-gray-200 bg-white px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-green-700"
                  >
                    <option value="">구·군 전체</option>
                    {sigunguList.map((sg) => (
                      <option key={sg} value={sg}>{sg}</option>
                    ))}
                  </select>
                  <span className="shrink-0 text-xs text-gray-400">{filteredVendors.length}개</span>
                </div>
              ) : (
                <p className="text-xs text-gray-500">
                  {selectedSido
                    ? `${selectedSido} · ${filteredVendors.length}개 업체`
                    : `전체 · ${filteredVendors.length}개 업체`}
                </p>
              )}
            </div>
            <div className="flex-1 overflow-y-auto p-3 space-y-2">
              {filteredVendors.length === 0 ? (
                <p className="py-10 text-center text-sm text-gray-400">
                  {selectedSido ? '해당 지역에 등록된 업체가 없습니다.' : '지도에서 지역을 선택하세요.'}
                </p>
              ) : (
                filteredVendors.map((v) => (
                  <button
                    key={v.id}
                    onClick={() => onSelect(v)}
                    className="w-full rounded-lg border bg-white p-3 text-left hover:border-green-500 hover:bg-green-50 transition-colors"
                  >
                    <p className="text-sm font-medium">{v.name}</p>
                    <div className="mt-1 flex flex-wrap gap-1">
                      {(v.capabilities ?? []).map((cap) => (
                        <span key={cap} className="rounded bg-gray-100 px-1.5 py-0.5 text-xs text-gray-600">
                          {CAP_LABEL[cap] ?? cap}
                        </span>
                      ))}
                    </div>
                    {(v.regions ?? []).length > 0 && (
                      <p className="mt-1 text-xs text-gray-400">
                        {(v.regions ?? [])
                          .slice(0, 3)
                          .map((r) => `${r.sido.replace(/(특별자치시|특별자치도|광역시|특별시|자치시|자치도|시|도)$/, '')} ${r.sigungu}`)
                          .join(' · ')}
                        {(v.regions ?? []).length > 3 && ` 외 ${(v.regions ?? []).length - 3}곳`}
                      </p>
                    )}
                  </button>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
