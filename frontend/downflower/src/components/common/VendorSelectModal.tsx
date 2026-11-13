'use client'

import { useState, useMemo, useEffect } from 'react'
import { ComposableMap, Geographies, Geography, Marker } from 'react-simple-maps'
import { geoCentroid } from 'd3-geo'
import { X } from 'lucide-react'
import { Vendor } from '@/types/vendor'
import { SIDO_LIST as SIDO_DATA } from '@/data/regions'
import { SIDO_VIEWPORT } from '@/data/sidoViewport'

const GEO_URL = '/korea-sido.json'
const SIGUNGU_GEO_URL = '/korea-sigungu.json'

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

// GeoJSON의 구 행정명 → regions.ts 현행 행정명 매핑
const SIDO_NAME_MAP: Record<string, string> = {
  '강원도': '강원특별자치도',
  '전라북도': '전북특별자치도',
}

const MAP_FILL = { base: '#ffffff', hover: '#f0fdf4', selected: '#15803d' }

const toCityName = (name: string) => name.replace(/^(.+?시)(.+구)$/, '$1')

// 레이블이 겹치는 시도의 텍스트 위치 보정 (SVG dx/dy, 단위: px)
const SIDO_LABEL_OFFSET: Record<string, [number, number]> = {
  '서울특별시':    [-12, -8],
  '경기도':       [ 32,  6],
  '인천광역시':   [-18,  0],
  '세종특별자치시': [ 0, -10],
  '대전광역시':   [  0,  10],
  '충청북도':     [  8, -8],
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
  const [mapLevel, setMapLevel] = useState<'national' | 'sido'>('national')

  const sigunguList = useMemo(() => {
    if (!selectedSido) return []
    const normalizedSido = SIDO_NAME_MAP[selectedSido] ?? selectedSido
    return SIDO_DATA.find((s) => s.name === normalizedSido)?.sigunguList ?? []
  }, [selectedSido])

  const filteredVendors = useMemo(() => {
    const normalizedSido = selectedSido ? (SIDO_NAME_MAP[selectedSido] ?? selectedSido) : null
    return vendors.filter((v) => {
      if (v.status !== 'ACTIVE') return false
      const regionMatch = normalizedSido
        ? (v.regions ?? []).some(
            (r) => r.sido === normalizedSido && (!selectedSigungu || r.sigungu === '전체' || r.sigungu === selectedSigungu)
          )
        : true
      const capMatch =
        selectedCapability === 'ALL' || (v.capabilities ?? []).includes(selectedCapability)
      return regionMatch && capMatch
    })
  }, [vendors, selectedSido, selectedSigungu, selectedCapability])

  useEffect(() => {
    if (!open) {
      setSelectedSido(null)
      setSelectedSigungu(null)
      setMapLevel('national')
    }
  }, [open])

  function handleSidoSelect(sido: string) {
    setSelectedSido(sido)
    setSelectedSigungu(null)
    setMapLevel('sido')
  }

  function handleBackToNational() {
    setSelectedSido(null)
    setSelectedSigungu(null)
    setMapLevel('national')
  }

  const normalizedSelectedSido = selectedSido ? (SIDO_NAME_MAP[selectedSido] ?? selectedSido) : null
  const sidoViewport = normalizedSelectedSido ? SIDO_VIEWPORT[normalizedSelectedSido] : null

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
        <div className="flex min-h-0 flex-1 overflow-hidden" style={{ minHeight: '760px' }}>
          {/* 지도 영역 */}
          <div className="flex flex-col border-r overflow-y-auto" style={{ width: '45%' }}>
            {/* PC: SVG 지도 */}
            <div className="hidden sm:flex flex-col overflow-hidden" style={{ height: '700px', flexShrink: 0 }}>
              {mapLevel === 'national' ? (
                <>
                  <div className="flex-1 min-h-0 overflow-hidden">
                    <ComposableMap
                      projection="geoMercator"
                      projectionConfig={{ center: [127.5, 36], scale: 3800 }}
                      width={430}
                      height={520}
                      style={{ width: '100%', height: '100%' }}
                    >
                      <Geographies geography={GEO_URL}>
                        {({ geographies }) => {
                          // eslint-disable-next-line @typescript-eslint/no-explicit-any
                          const geos = geographies as any[]
                          return (
                            <>
                              {geos.map((geo) => {
                                const sido = (geo.properties as { name?: string })?.name ?? ''
                                const isSelected = selectedSido === sido
                                return (
                                  <Geography
                                    key={geo.rsmKey}
                                    geography={geo}
                                    onClick={() => handleSidoSelect(sido)}
                                    fill={isSelected ? MAP_FILL.selected : MAP_FILL.base}
                                    stroke="#9ca3af"
                                    strokeWidth={0.7}
                                    style={{ cursor: 'pointer', outline: 'none' }}
                                    onMouseEnter={(e) => {
                                      if (!isSelected) (e.target as SVGPathElement).setAttribute('fill', MAP_FILL.hover)
                                    }}
                                    onMouseLeave={(e) => {
                                      if (!isSelected) (e.target as SVGPathElement).setAttribute('fill', MAP_FILL.base)
                                    }}
                                  />
                                )
                              })}
                              {geos.map((geo) => {
                                const sido = (geo.properties as { name?: string })?.name ?? ''
                                const isSelected = selectedSido === sido
                                const centroid = geoCentroid(geo)
                                const shortName = sido
                                  .replace('특별자치시', '').replace('특별자치도', '')
                                  .replace('광역시', '').replace('특별시', '')
                                  .replace('자치시', '').replace('자치도', '')
                                const [dx, dy] = SIDO_LABEL_OFFSET[sido] ?? [0, 0]
                                return (
                                  <Marker key={geo.rsmKey + '-label'} coordinates={centroid}>
                                    <text
                                      fontSize={10}
                                      fontWeight="600"
                                      textAnchor="middle"
                                      dominantBaseline="central"
                                      dx={dx}
                                      dy={dy}
                                      fill={isSelected ? '#fff' : '#1f2937'}
                                      stroke={isSelected ? 'none' : '#fff'}
                                      strokeWidth={2.5}
                                      paintOrder="stroke"
                                      style={{ pointerEvents: 'none', userSelect: 'none' }}
                                    >
                                      {shortName}
                                    </text>
                                  </Marker>
                                )
                              })}
                            </>
                          )
                        }}
                      </Geographies>
                    </ComposableMap>
                  </div>
                </>
              ) : (
                <>
                  <button
                    onClick={handleBackToNational}
                    className="flex items-center gap-1 border-b px-3 py-1.5 text-xs text-green-700 hover:bg-green-50 w-full shrink-0"
                  >
                    ← 전국지도보기
                  </button>
                  <div className="flex-1 min-h-0 overflow-hidden">
                    <ComposableMap
                      projection="geoMercator"
                      projectionConfig={
                        sidoViewport
                          ? { center: sidoViewport.center, scale: sidoViewport.scale }
                          : { center: [127.5, 36], scale: 3800 }
                      }
                      width={430}
                      height={500}
                      style={{ width: '100%', height: '100%' }}
                    >
                      <Geographies geography={SIGUNGU_GEO_URL}>
                        {({ geographies }) => {
                          // eslint-disable-next-line @typescript-eslint/no-explicit-any
                          const filtered = geographies.filter((geo: any) => geo.properties.sido === normalizedSelectedSido)
                          const labeledCities = new Set<string>()
                          const cityCentroids = new Map<string, [number, number][]>()
                          // eslint-disable-next-line @typescript-eslint/no-explicit-any
                          filtered.forEach((geo: any) => {
                            const city = toCityName((geo.properties as { name?: string })?.name ?? '')
                            const list = cityCentroids.get(city) ?? []
                            list.push(geoCentroid(geo))
                            cityCentroids.set(city, list)
                          })
                          return (
                            <>
                              {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
                              {filtered.map((geo: any) => {
                                const sigungu = toCityName((geo.properties as { name?: string })?.name ?? '')
                                const isSelected = selectedSigungu === sigungu
                                return (
                                  <Geography
                                    key={geo.rsmKey}
                                    geography={geo}
                                    onClick={() => setSelectedSigungu(sigungu === selectedSigungu ? null : sigungu)}
                                    fill={isSelected ? MAP_FILL.selected : MAP_FILL.base}
                                    stroke="#9ca3af"
                                    strokeWidth={0.7}
                                    style={{ cursor: 'pointer', outline: 'none' }}
                                    onMouseEnter={(e) => {
                                      if (!isSelected) (e.target as SVGPathElement).setAttribute('fill', MAP_FILL.hover)
                                    }}
                                    onMouseLeave={(e) => {
                                      if (!isSelected) (e.target as SVGPathElement).setAttribute('fill', MAP_FILL.base)
                                    }}
                                  />
                                )
                              })}
                              {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
                              {filtered.map((geo: any) => {
                                const sigungu = toCityName((geo.properties as { name?: string })?.name ?? '')
                                if (labeledCities.has(sigungu)) return null
                                labeledCities.add(sigungu)
                                const isSelected = selectedSigungu === sigungu
                                const points = cityCentroids.get(sigungu) ?? []
                                const centroid: [number, number] = [
                                  points.reduce((s, p) => s + p[0], 0) / points.length,
                                  points.reduce((s, p) => s + p[1], 0) / points.length,
                                ]
                                return (
                                  <Marker key={geo.rsmKey + '-label'} coordinates={centroid}>
                                    <text
                                      fontSize={7}
                                      fontWeight="600"
                                      textAnchor="middle"
                                      dominantBaseline="central"
                                      fill={isSelected ? '#fff' : '#1f2937'}
                                      stroke={isSelected ? 'none' : '#fff'}
                                      strokeWidth={2.5}
                                      paintOrder="stroke"
                                      style={{ pointerEvents: 'none', userSelect: 'none' }}
                                    >
                                      {sigungu}
                                    </text>
                                  </Marker>
                                )
                              })}
                            </>
                          )
                        }}
                      </Geographies>
                    </ComposableMap>
                  </div>
                </>
              )}
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
                filteredVendors.map((v) => {
                  const isClosed = v.businessStatus === 'CLOSED'
                  return (
                    <button
                      key={v.id}
                      onClick={() => !isClosed && onSelect(v)}
                      disabled={isClosed}
                      className={`w-full rounded-lg border p-3 text-left transition-colors ${
                        isClosed
                          ? 'cursor-not-allowed bg-gray-50 opacity-60'
                          : 'bg-white hover:border-green-500 hover:bg-green-50'
                      }`}
                    >
                      <div className="flex items-center gap-2">
                        <p className="text-sm font-medium">{v.name}</p>
                        {isClosed && (
                          <span className="rounded bg-gray-200 px-1.5 py-0.5 text-xs text-gray-500">휴무중</span>
                        )}
                      </div>
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
                  )
                })
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
