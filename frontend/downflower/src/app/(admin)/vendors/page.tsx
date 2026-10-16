import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { mockVendors } from '@/lib/mock/vendors'

export default function VendorsPage() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold">업체계정관리</h1>
        <Button className="bg-green-700 hover:bg-green-800">업체 등록</Button>
      </div>

      <div className="rounded-lg border bg-white">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>업체명</TableHead>
              <TableHead>담당자명</TableHead>
              <TableHead>연락처</TableHead>
              <TableHead>주소</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>관리</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {mockVendors.map((vendor) => (
              <TableRow key={vendor.id}>
                <TableCell className="font-medium">{vendor.name}</TableCell>
                <TableCell className="text-sm">{vendor.managerName}</TableCell>
                <TableCell className="text-sm">{vendor.phone}</TableCell>
                <TableCell className="text-sm">{vendor.address}</TableCell>
                <TableCell>
                  <Badge
                    variant={vendor.status === 'active' ? 'default' : 'destructive'}
                    className={vendor.status === 'active' ? 'bg-green-100 text-green-800 hover:bg-green-100' : ''}
                  >
                    {vendor.status === 'active' ? '활성' : '정지'}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div className="flex gap-1">
                    <button className="text-xs text-blue-600 hover:underline">수정</button>
                    <span className="text-gray-300">|</span>
                    <button
                      className={`text-xs ${vendor.status === 'active' ? 'text-red-600' : 'text-green-600'} hover:underline`}
                    >
                      {vendor.status === 'active' ? '정지' : '활성화'}
                    </button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </div>
  )
}
