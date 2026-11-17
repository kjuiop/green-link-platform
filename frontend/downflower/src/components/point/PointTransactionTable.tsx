'use client'

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { PointTransaction } from '@/lib/api/pointApi'

export const formatPoint = (n: number) => n.toLocaleString('ko-KR') + ' P'

function formatDateTime(s: string) {
  return s.replace('T', ' ').slice(0, 16)
}

export function PointTransactionTable({
  transactions,
  loading,
}: {
  transactions: PointTransaction[]
  loading: boolean
}) {
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
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow>
              <TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">불러오는 중...</TableCell>
            </TableRow>
          ) : transactions.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="py-10 text-center text-sm text-gray-400">포인트 이력이 없습니다.</TableCell>
            </TableRow>
          ) : (
            transactions.map((t) => {
              const isCharge = t.type === 'CHARGE'
              return (
                <TableRow key={t.id}>
                  <TableCell className="text-sm">{formatDateTime(t.createdAt)}</TableCell>
                  <TableCell>
                    <Badge
                      variant="outline"
                      className={isCharge
                        ? 'border-green-200 bg-green-50 text-green-700'
                        : 'border-orange-200 bg-orange-50 text-orange-700'}
                    >
                      {isCharge ? '충전' : '사용'}
                    </Badge>
                  </TableCell>
                  <TableCell className={`text-right text-sm font-medium ${isCharge ? 'text-green-700' : 'text-orange-600'}`}>
                    {isCharge ? '+' : '-'}{formatPoint(t.amount)}
                  </TableCell>
                  <TableCell className="text-right text-sm text-gray-700">{formatPoint(t.balanceAfter)}</TableCell>
                  <TableCell className="text-sm text-gray-600">{t.memberLoginId ?? (isCharge ? '관리자' : '-')}</TableCell>
                  <TableCell className="text-sm text-gray-600">{t.memo ?? '-'}</TableCell>
                </TableRow>
              )
            })
          )}
        </TableBody>
      </Table>
    </div>
  )
}
