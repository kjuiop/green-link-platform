'use client'

import { useEffect } from 'react'
import { mockOrders } from '@/lib/mock/orders'
import { notFound } from 'next/navigation'
import { use } from 'react'

interface Props {
  params: Promise<{ id: string }>
}

export default function PrintOrderPage({ params }: Props) {
  const { id } = use(params)
  const order = mockOrders.find((o) => o.id === id)
  if (!order) notFound()

  useEffect(() => {
    const customer = order.recipientName || order.ordererName
    document.title = `${customer}_${order.placeName}_${order.deliveryAt}`
  }, [order])

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

      <div className="print-page" style={{ paddingTop: 40 }}>
        {/* 주문서 */}
        <div className="order-num">주문번호 {order.orderNumber}</div>
        <table>
          <tbody>
            <tr>
              <td colSpan={2}><h2 style={{ margin: 0, marginLeft: 20 }}>주문서</h2></td>
              <td className="left" style={{ fontWeight: 'normal', fontSize: 16 }}>
                본부전화: 다운플라워<br />
                http://downflower.co.kr
              </td>
            </tr>
            <tr>
              <td>발주화원</td>
              <td>다운플라워</td>
              <td>TEL: —</td>
            </tr>
            <tr>
              <td>수주화원</td>
              <td>{order.vendorName}</td>
              <td>TEL: —</td>
            </tr>
            <tr>
              <td>상품명</td>
              <td>{order.productName} × {order.quantity}</td>
              <td>발주금액: {order.amount.toLocaleString()}원</td>
            </tr>
            <tr>
              <td>배달정보</td>
              <td colSpan={2} className="left">
                배달주소: {order.address} {order.addressDetail}<br />
                장소: {order.placeName} {order.roomName}<br />
                배달일시: {order.deliveryAt}
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
              <td colSpan={2} className="left" style={{ fontWeight: 'normal', minHeight: 50 }}>
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
                본부전화: 다운플라워<br />
                http://downflower.co.kr
              </td>
            </tr>
            <tr>
              <td>발주화원</td>
              <td>다운플라워</td>
              <td>TEL: —</td>
            </tr>
            <tr>
              <td>수주화원</td>
              <td>{order.vendorName}</td>
              <td>TEL: —</td>
            </tr>
            <tr>
              <td>상품명</td>
              <td colSpan={2}>{order.productName} × {order.quantity}</td>
            </tr>
            <tr>
              <td>배달정보</td>
              <td colSpan={2} className="left">
                배달주소: {order.address} {order.addressDetail}<br />
                배달일시: {order.deliveryAt}
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
              <td colSpan={2} className="left" style={{ fontWeight: 'normal', minHeight: 50 }}>
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
