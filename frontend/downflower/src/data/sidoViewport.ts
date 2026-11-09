export interface SidoViewport {
  center: [number, number]
  scale: number
}

export const SIDO_VIEWPORT: Record<string, SidoViewport> = {
  '서울특별시':     { center: [126.98, 37.56], scale: 55000 },
  '부산광역시':     { center: [129.05, 35.18], scale: 30000 },
  '대구광역시':     { center: [128.60, 35.87], scale: 30000 },
  '인천광역시':     { center: [126.63, 37.47], scale: 25000 },
  '광주광역시':     { center: [126.85, 35.16], scale: 40000 },
  '대전광역시':     { center: [127.39, 36.35], scale: 40000 },
  '울산광역시':     { center: [129.31, 35.54], scale: 32000 },
  '세종특별자치시':  { center: [127.25, 36.48], scale: 60000 },
  '경기도':        { center: [127.20, 37.65], scale: 16000 },
  '강원특별자치도':  { center: [128.20, 37.65], scale: 8000  },
  '충청북도':      { center: [128.00, 36.70], scale: 15000 },
  '충청남도':      { center: [126.80, 36.65], scale: 11000 },
  '전북특별자치도':  { center: [127.15, 35.72], scale: 11000 },
  '전라남도':      { center: [126.99, 34.80], scale: 9000  },
  '경상북도':      { center: [128.89, 36.57], scale: 7500  },
  '경상남도':      { center: [128.25, 35.46], scale: 10000 },
  '제주특별자치도':  { center: [126.50, 33.38], scale: 25000 },
}
