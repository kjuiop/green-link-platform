'use client'

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { cn } from '@/lib/utils'

type EventType = '장례식' | '결혼식' | '기타'

const EVENT_TABS: EventType[] = ['장례식', '결혼식', '기타']

const DEFAULT_TEMPLATES: Record<EventType, string> = {
  장례식: `안녕하세요, {주문자명}님.
삼가 고인의 명복을 빕니다.
주문하신 근조화환이 {장소}에 배송 완료되었습니다.

[배송사진 확인]
{사진링크}

감사합니다.
다운플라워 드림`,
  결혼식: `안녕하세요, {주문자명}님.
주문하신 축하화환이 {장소}에 배송 완료되었습니다.
결혼을 진심으로 축하드립니다 🎊

[배송사진 확인]
{사진링크}

감사합니다.
다운플라워 드림`,
  기타: `안녕하세요, {주문자명}님.
주문하신 화환이 {장소}에 배송 완료되었습니다.

[배송사진 확인]
{사진링크}

감사합니다.
다운플라워 드림`,
}

const VARIABLES = [
  { key: '{주문자명}', desc: '주문자 이름' },
  { key: '{장소}', desc: '배송 장소명' },
  { key: '{사진링크}', desc: '배송사진 링크 (자동 삽입)' },
  { key: '{주문번호}', desc: '주문번호' },
]

export default function SettingsMessagesPage() {
  const [activeTab, setActiveTab] = useState<EventType>('장례식')
  const [templates, setTemplates] = useState<Record<EventType, string>>(DEFAULT_TEMPLATES)
  const [saved, setSaved] = useState(false)

  function handleSave() {
    // TODO: 저장 API 호출
    setSaved(true)
    setTimeout(() => setSaved(false), 2000)
  }

  function insertVariable(key: string) {
    setTemplates((prev) => ({
      ...prev,
      [activeTab]: prev[activeTab] + key,
    }))
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">문자 설정</h1>

      {/* 발신 정보 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">발신 정보</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label>발신번호</Label>
            <Input placeholder="010-0000-0000" />
          </div>
          <div className="space-y-1.5">
            <Label>발신자명</Label>
            <Input placeholder="다운플라워" />
          </div>
        </CardContent>
      </Card>

      {/* 이벤트별 문자 템플릿 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">배송완료 문자 템플릿</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* 탭 */}
          <div className="flex gap-1 rounded-lg border bg-gray-50 p-1">
            {EVENT_TABS.map((tab) => (
              <button
                key={tab}
                type="button"
                onClick={() => setActiveTab(tab)}
                className={cn(
                  'flex-1 rounded-md py-1.5 text-sm font-medium transition-colors',
                  activeTab === tab
                    ? 'bg-white text-green-700 shadow-sm'
                    : 'text-gray-500 hover:text-gray-700',
                )}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* 변수 안내 */}
          <div className="flex flex-wrap gap-2">
            {VARIABLES.map(({ key, desc }) => (
              <button
                key={key}
                type="button"
                onClick={() => insertVariable(key)}
                title={desc}
                className="rounded border border-dashed border-gray-300 bg-gray-50 px-2 py-0.5 font-mono text-xs text-gray-600 hover:border-green-500 hover:text-green-700"
              >
                {key}
              </button>
            ))}
            <span className="self-center text-xs text-gray-400">클릭하면 커서 위치에 삽입</span>
          </div>

          {/* 템플릿 입력 */}
          <div className="space-y-1.5">
            <Label>{activeTab} 문자 내용</Label>
            <Textarea
              key={activeTab}
              rows={8}
              value={templates[activeTab]}
              onChange={(e) =>
                setTemplates((prev) => ({ ...prev, [activeTab]: e.target.value }))
              }
              className="font-mono text-sm"
            />
            <p className="text-xs text-gray-400">
              {'{사진링크}'}는 배송완료 처리 시 자동으로 실제 링크로 교체됩니다.
            </p>
          </div>

          <Button
            onClick={handleSave}
            className={cn(
              'min-w-20',
              saved ? 'bg-green-600 hover:bg-green-700' : 'bg-green-700 hover:bg-green-800',
            )}
          >
            {saved ? '저장됨 ✓' : '저장'}
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}
