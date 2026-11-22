import { Card, CardContent } from '@/components/ui/card'

export default function ComingSoonPage() {
  return (
    <Card>
      <CardContent className="py-12 text-center">
        <p className="text-base font-medium">준비 중입니다</p>
        <p className="mt-1 text-sm text-gray-500">이 기능은 이후 단계에서 제공됩니다.</p>
      </CardContent>
    </Card>
  )
}
