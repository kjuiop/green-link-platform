'use client'

import type { ReactNode } from 'react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { PointTransaction } from '@/lib/api/pointApi'

export const formatPoint = (n: number) => n.toLocaleString('ko-KR') + ' P'

const TYPE_STYLE: Record<PointTransaction['type'], { label: string; badge: string; text: string; sign: string }> = {
  CHARGE: { label: '충전', badge: 'border-green-200 bg-green-50 text-green-700', text: 'text-green-700', sign: '+' },
  USE: { label: '예치', badge: 'border-orange-200 bg-orange-50 text-orange-700', text: 'text-orange-600', sign: '-' },
  REFUND: { label: '환불', badge: 'border-blue-200 bg-blue-50 text-blue-700', text: 'text-blue-600', sign: '+' },
  SETTLE: { label: '정산', badge: 'border-gray-200 bg-gray-50 text-gray-600', text: 'text-gray-500', sign: '' },
}

function formatDateTime(s: string) {
  return s.replace('T', ' ').slice(0, 16)
}

export function PointTransactionTable({
  transactions,
  loading,
  renderInvoice,
}: {
  transactions: PointTransaction[]
  loading: boolean
  renderInvoice?: (t: PointTransaction) => ReactNode
}) {
  const colCount = renderInvoice ? 7 : 6
  return (
    <div className="rounded-lg border bg-white">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>일시</TableHead>
            <TableHead>구분</TableHead>
            <TableHead className="text-right">금액</TableHead>
            <TableHead className="text-right">잔액</TableHead>
            <TableHead>사용 계정</TableHead>
            <TableHead>내역</TableHead>
            {renderInvoice && <TableHead>계산서</TableHead>}
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow>
              <TableCell colSpan={colCount} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
            </TableRow>
          ) : transactions.length === 0 ? (
            <TableRow>
              <TableCell colSpan={colCount} className="py-10 text-center text-sm text-gray-400">포인트 이력이 없습니다.</TableCell>
            </TableRow>
          ) : (
            transactions.map((t) => {
              const style = TYPE_STYLE[t.type]
              return (
                <TableRow key={t.id}>
                  <TableCell className="text-sm">{formatDateTime(t.createdAt)}</TableCell>
                  <TableCell>
                    <Badge
                      variant="outline"
                      className={style.badge}
                    >
                      {style.label}
                    </Badge>
                  </TableCell>
                  <TableCell className={`text-right text-sm font-medium ${style.text}`}>
                    {style.sign}{formatPoint(t.amount)}
                  </TableCell>
                  <TableCell className="text-right text-sm text-gray-700">{formatPoint(t.balanceAfter)}</TableCell>
                  <TableCell className="text-sm text-gray-600">{t.memberLoginId ?? (t.type === 'CHARGE' ? '관리자' : '-')}</TableCell>
                  <TableCell className="text-sm text-gray-600">{t.memo ?? '-'}</TableCell>
                  {renderInvoice && <TableCell>{renderInvoice(t)}</TableCell>}
                </TableRow>
              )
            })
          )}
        </TableBody>
      </Table>
    </div>
  )
}
