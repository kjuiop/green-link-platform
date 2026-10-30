'use client'

import { useState, useEffect } from 'react'
import { useParams } from 'next/navigation'
import { fetchOrder, updateOrderStatus } from '@/lib/api/orderApi'
import { Order } from '@/types/order'

function buildDeliveryAddress(order: Order): string {
  const parts: string[] = []
  const addr = [order.address, order.addressDetail].filter(Boolean).join(' ')
  if (addr) parts.push(addr)
  const place = [order.placeName, order.roomName].filter(Boolean).join(' ')
  if (place) parts.push(place)
  return parts.join(' / ')
}

function buildDeliveryDateTime(order: Order): string {
  if (!order.deliveryAt) return ''
  let result = order.deliveryAt
  const type = order.deliveryTimeType
  if (type === '3시간이내') {
    result += ' 3시간이내'
  } else if (type === '직접입력' && order.deliveryTimeCustom) {
    result += ' ' + order.deliveryTimeCustom
  } else if ((type === '까지' || type === '예식' || type === '행사') && order.deliveryTimeHour && order.deliveryTimeMinute) {
    result += ` ${order.deliveryTimeHour}:${order.deliveryTimeMinute}`
    if (type !== '까지') result += ' ' + type
    else result += '까지'
  }
  return result
}

export default function PrintOrderPage() {
  const { id } = useParams<{ id: string }>()
  const [order, setOrder] = useState<Order | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [accepting, setAccepting] = useState(false)
  const [acceptError, setAcceptError] = useState<string | null>(null)

  useEffect(() => {
    fetchOrder(id)
      .then((o) => {
        setOrder(o)
        const customer = o.recipientName || o.ordererName
        document.title = `${customer}_${o.placeName}_${o.deliveryAt}`
      })
      .catch(() => setError('주문 정보를 불러오지 못했습니다.'))
  }, [id])

  async function handleAccept() {
    setAccepting(true)
    setAcceptError(null)
    try {
      const updated = await updateOrderStatus(id, 'accept')
      setOrder(updated)
    } catch {
      setAcceptError('수주 승인에 실패했습니다. 다시 시도해 주세요.')
    } finally {
      setAccepting(false)
    }
  }

  if (error) return <div style={{ padding: 40, color: 'red' }}>{error}</div>
  if (!order) return <div style={{ padding: 40, color: '#999' }}>불러오는 중...</div>

  return (
    <>
      <style>{`
        @page { size: A4 portrait; margin: 10mm; }
        @media print {
          .no-print { display: none !important; }
          body { margin: 0; }
          .print-page { width: 100%; margin: 0; }
          .divider { margin: 30px 0; }
        }
        body { font-family: '맑은 고딕', sans-serif; background: #fff; }
        .print-page { width: 90%; margin: 0 auto; }
        table { width: 100%; border-collapse: collapse; }
        td { border: 2px solid #000; text-align: center; font-size: 20px; font-weight: 600; padding: 0; }
        td.left { text-align: left; padding-left: 10px; }
        .divider { border: none; border-top: 1px dotted #000; margin: 50px 0; }
        .order-num { text-align: left; font-size: 18px; margin-bottom: 4px; }
        .title-cell { font-size: 20px !important; }
      `}</style>

      <div className="no-print fixed top-4 right-4 z-10">
        <button
          onClick={() => window.print()}
          className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800"
        >
          인쇄
        </button>
      </div>

      <div className="no-print fixed bottom-4 left-1/2 z-10 flex -translate-x-1/2 gap-2">
        <button
          onClick={() => window.print()}
          className="rounded-md bg-blue-700 px-4 py-2 text-sm font-medium text-white hover:bg-blue-800"
        >
          인수증 출력
        </button>
        <button
          onClick={() => window.close()}
          className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          창 닫기
        </button>
        {order?.status === '발주대기' && (
          <button
            onClick={handleAccept}
            disabled={accepting}
            className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50"
          >
            {accepting ? '처리 중…' : '미확인'}
          </button>
        )}
        {order?.status === '수주완료' && order.acceptedAt && (
          <span className="flex items-center rounded-md bg-blue-50 px-3 py-2 text-xs text-blue-700">
            수주완료 · {order.acceptedAt.slice(0, 16).replace('T', ' ')}
          </span>
        )}
      </div>
      {acceptError && (
        <div className="no-print fixed bottom-16 left-1/2 -translate-x-1/2 rounded-md border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600">
          {acceptError}
        </div>
      )}

      <div className="print-page" style={{ paddingTop: 40 }}>
        {/* 주문서 */}
        <div className="order-num">주문번호 {order.orderNumber}</div>
        <table>
          <tbody>
            <tr>
              <td colSpan={2}><h2 style={{ margin: 0, marginLeft: 20 }}>주문서</h2></td>
              <td className="left" style={{ fontWeight: 'normal', fontSize: 16 }}>
                본부전화: {order.orderingCompanyName || '—'}<br />
                http://downflower.co.kr
              </td>
            </tr>
            <tr>
              <td>발주화원</td>
              <td>{order.orderingCompanyName || '—'}</td>
              <td>TEL: {order.orderingCompanyPhone || '—'}</td>
            </tr>
            <tr>
              <td>수주화원</td>
              <td>{order.vendorName}</td>
              <td>TEL: {order.vendorPhone || '—'}</td>
            </tr>
            <tr>
              <td>상품명</td>
              <td>{order.productName} × {order.quantity}</td>
              <td>발주금액: {(order.amount + (order.extraCost ?? 0)).toLocaleString()}원</td>
            </tr>
            <tr>
              <td>배달정보</td>
              <td colSpan={2} className="left" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                배달주소: {buildDeliveryAddress(order)}<br />
                배달일시: {buildDeliveryDateTime(order)}
              </td>
            </tr>
            <tr>
              <td>받는 고객명</td>
              <td>{order.recipientName || order.ordererName}</td>
              <td className="left" style={{ fontWeight: 'normal' }}>
                HP: {order.recipientPhone || order.ordererPhone}
              </td>
            </tr>
            <tr>
              <td>리본</td>
              <td colSpan={2} className="left">
                경조사어: {order.ribbonRight}<br />
                보내시는 분: {order.ribbonLeft}
              </td>
            </tr>
            {order.eventInfo && (
              <tr>
                <td>행사정보</td>
                <td colSpan={2} className="left">{order.eventInfo}</td>
              </tr>
            )}
            <tr>
              <td>요청사항</td>
              <td colSpan={2} className="left" style={{ fontWeight: 'normal', height: 50 }}>
                {order.vendorMemo || ''}
              </td>
            </tr>
          </tbody>
        </table>

        <hr className="divider" />

        {/* 인수증 */}
        <div className="order-num">주문번호 {order.orderNumber}</div>
        <table>
          <tbody>
            <tr>
              <td colSpan={2}><h2 style={{ margin: 0, marginLeft: 20 }}>인수증</h2></td>
              <td className="left" style={{ fontWeight: 'normal', fontSize: 16 }}>
                본부전화: {order.orderingCompanyName || '—'}<br />
                http://downflower.co.kr
              </td>
            </tr>
            <tr>
              <td>발주화원</td>
              <td>{order.orderingCompanyName || '—'}</td>
              <td>TEL: {order.orderingCompanyPhone || '—'}</td>
            </tr>
            <tr>
              <td>수주화원</td>
              <td>{order.vendorName}</td>
              <td>TEL: {order.vendorPhone || '—'}</td>
            </tr>
            <tr>
              <td>상품명</td>
              <td colSpan={2}>{order.productName} × {order.quantity}</td>
            </tr>
            <tr>
              <td>배달정보</td>
              <td colSpan={2} className="left" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                배달주소: {buildDeliveryAddress(order)}<br />
                배달일시: {buildDeliveryDateTime(order)}
              </td>
            </tr>
            <tr>
              <td>받는 고객명</td>
              <td>{order.recipientName || order.ordererName}</td>
              <td className="left" style={{ fontWeight: 'normal' }}>
                HP: {order.recipientPhone || order.ordererPhone}
              </td>
            </tr>
            <tr>
              <td>리본</td>
              <td colSpan={2} className="left">
                경조사어: {order.ribbonRight}<br />
                보내시는 분: {order.ribbonLeft}
              </td>
            </tr>
            <tr>
              <td>요청사항</td>
              <td colSpan={2} className="left" style={{ fontWeight: 'normal', height: 50 }}>
                {order.vendorMemo || ''}
              </td>
            </tr>
          </tbody>
        </table>

        <table style={{ marginTop: 10 }}>
          <tbody>
            <tr>
              <td width="15%">인수자</td>
              <td width="25%"></td>
              <td width="15%">관계</td>
              <td width="25%"></td>
              <td width="20%">인수시간</td>
            </tr>
          </tbody>
        </table>
      </div>
    </>
  )
}
