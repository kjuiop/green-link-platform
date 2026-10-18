'use client'

import { useState, useRef, useEffect } from 'react'
import { type MenuItem, type UserRole, ALL_ROLES, ROLE_LABELS } from '@/types/menu'
import { fetchAllMenus, createMenu, updateMenu, deleteMenu } from '@/lib/api/menuApi'
import { ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'

type FormState = {
  id: number | null
  parentId: number | null
  name: string
  url: string
  parentUrl: string
  sortOrder: number
  roles: UserRole[]
}

const EMPTY_FORM: FormState = {
  id: null,
  parentId: null,
  name: '',
  url: '',
  parentUrl: '',
  sortOrder: 0,
  roles: [],
}

export default function MenuManagePage() {
  const nextIdRef = useRef(100)
  const [menus, setMenus] = useState<MenuItem[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [inactiveSelected, setInactiveSelected] = useState<UserRole | null>(null)
  const [activeSelected, setActiveSelected] = useState<UserRole | null>(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchAllMenus()
      .then(setMenus)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  function selectMenu(menu: MenuItem, parent?: MenuItem) {
    setError('')
    setInactiveSelected(null)
    setActiveSelected(null)
    setForm({
      id: menu.id,
      parentId: parent?.id ?? null,
      name: menu.name,
      url: parent ? menu.url.replace(parent.url === '/' ? '' : parent.url, '') : menu.url,
      parentUrl: parent?.url ?? '',
      sortOrder: menu.sortOrder,
      roles: [...menu.roles],
    })
  }

  function startNewChild() {
    if (!form.id) return
    const fullParentUrl = form.parentUrl
      ? form.parentUrl.replace(/\/$/, '') + '/' + form.url.replace(/^\//, '')
      : form.url
    setForm({ ...EMPTY_FORM, parentId: form.id, parentUrl: fullParentUrl })
  }

  async function handleSave() {
    if (!form.name.trim()) { setError('이름을 입력하세요.'); return }
    if (!form.url.trim()) { setError('URL을 입력하세요.'); return }
    setSaving(true)
    setError('')

    const fullUrl = form.parentUrl
      ? form.parentUrl.replace(/\/$/, '') + '/' + form.url.replace(/^\//, '')
      : form.url

    try {
      if (form.id) {
        await updateMenu(form.id, {
          name: form.name,
          url: fullUrl,
          sortOrder: form.sortOrder,
          roles: form.roles,
        })
      } else {
        await createMenu({
          name: form.name,
          url: fullUrl,
          sortOrder: form.sortOrder,
          parentId: form.parentId,
          roles: form.roles,
        })
      }
      const updated = await fetchAllMenus()
      setMenus(updated)
      setForm(EMPTY_FORM)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete() {
    if (!form.id || !confirm('정말 삭제하시겠습니까?')) return
    setSaving(true)
    try {
      await deleteMenu(form.id)
      const updated = await fetchAllMenus()
      setMenus(updated)
      setForm(EMPTY_FORM)
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : '삭제에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  function includeRole() {
    if (!inactiveSelected) return
    setForm((prev) => ({ ...prev, roles: [...prev.roles, inactiveSelected] }))
    setInactiveSelected(null)
  }

  function excludeRole() {
    if (!activeSelected) return
    setForm((prev) => ({ ...prev, roles: prev.roles.filter((r) => r !== activeSelected) }))
    setActiveSelected(null)
  }

  const inactiveRoles = ALL_ROLES.filter((r) => !form.roles.includes(r))
  const activeRoles = ALL_ROLES.filter((r) => form.roles.includes(r))

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold">메뉴 관리</h1>
        <p className="mt-0.5 text-sm text-gray-500">사이드바 메뉴 구성 및 역할별 접근 권한을 관리합니다.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {/* 메뉴 트리 */}
        <div className="rounded-lg border bg-white">
          <div className="border-b px-4 py-3">
            <h2 className="text-sm font-semibold">메뉴 트리</h2>
          </div>
          <div className="p-3">
            {loading ? (
              <p className="px-3 py-4 text-center text-sm text-gray-400">불러오는 중...</p>
            ) : (
              <ul className="space-y-0.5">
                {[...menus].sort((a, b) => a.sortOrder - b.sortOrder).map((menu) => (
                  <li key={menu.id}>
                    <button
                      onClick={() => selectMenu(menu)}
                      className={cn(
                        'w-full rounded px-3 py-2 text-left text-sm transition-colors',
                        form.id === menu.id && !form.parentId
                          ? 'bg-green-50 font-medium text-green-700'
                          : 'text-gray-600 hover:bg-gray-50',
                      )}
                    >
                      {menu.name}
                    </button>
                    {(menu.children?.length ?? 0) > 0 && (
                      <ul className="ml-4 border-l pl-2">
                        {[...menu.children!].sort((a, b) => a.sortOrder - b.sortOrder).map((child) => (
                          <li key={child.id}>
                            <button
                              onClick={() => selectMenu(child, menu)}
                              className={cn(
                                'flex w-full items-center gap-1 rounded px-3 py-1.5 text-left text-sm transition-colors',
                                form.id === child.id
                                  ? 'bg-green-50 font-medium text-green-700'
                                  : 'text-gray-500 hover:bg-gray-50',
                              )}
                            >
                              <ChevronRight className="h-3 w-3 shrink-0 text-gray-300" />
                              {child.name}
                            </button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                ))}
              </ul>
            )}
            <button
              onClick={() => setForm(EMPTY_FORM)}
              className="mt-3 w-full rounded-md border border-dashed px-3 py-2 text-sm text-gray-400 hover:bg-gray-50 hover:text-gray-600"
            >
              + 최상위 메뉴 추가
            </button>
          </div>
        </div>

        {/* 상세 폼 */}
        <div className="rounded-lg border bg-white lg:col-span-2">
          <div className="border-b px-4 py-3">
            <h2 className="text-sm font-semibold">
              {form.id ? '메뉴 수정' : form.parentId ? '하위 메뉴 추가' : '메뉴 추가'}
            </h2>
          </div>
          <div className="space-y-4 p-4">
            {/* 이름 */}
            <div className="space-y-1">
              <label className="text-xs font-medium text-gray-500">이름</label>
              <input
                type="text"
                placeholder="메뉴 이름"
                value={form.name}
                onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
                className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
              />
            </div>

            {/* URL */}
            <div className="space-y-1">
              <label className="text-xs font-medium text-gray-500">URL</label>
              <div className="flex items-center gap-1.5">
                {form.parentUrl && (
                  <span className="rounded-md border bg-gray-50 px-2.5 py-2 text-sm text-gray-500">
                    {form.parentUrl}
                  </span>
                )}
                <input
                  type="text"
                  placeholder={form.parentUrl ? 'path' : '/path'}
                  value={form.url}
                  onChange={(e) => setForm((p) => ({ ...p, url: e.target.value }))}
                  className="flex-1 rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
                />
              </div>
            </div>

            {/* 정렬 순서 */}
            <div className="space-y-1">
              <label className="text-xs font-medium text-gray-500">정렬 순서</label>
              <input
                type="number"
                value={form.sortOrder}
                onChange={(e) => setForm((p) => ({ ...p, sortOrder: Number(e.target.value) }))}
                className="w-24 rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-700"
              />
            </div>

            {/* 접근 권한 */}
            <div className="space-y-2">
              <label className="text-xs font-medium text-gray-500">접근 권한</label>
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                {/* 비활성 권한 */}
                <div className="flex-1">
                  <p className="mb-1 text-center text-xs text-gray-400">비활성</p>
                  <div className="h-24 overflow-y-auto rounded-md border bg-gray-50">
                    {inactiveRoles.map((role) => (
                      <div
                        key={role}
                        onClick={() => { setInactiveSelected(role); setActiveSelected(null) }}
                        className={cn(
                          'cursor-pointer px-3 py-2 text-sm transition-colors',
                          inactiveSelected === role
                            ? 'bg-green-50 font-medium text-green-700'
                            : 'text-gray-500 hover:bg-gray-100',
                        )}
                      >
                        {ROLE_LABELS[role]}
                      </div>
                    ))}
                  </div>
                </div>

                {/* 이동 버튼 */}
                <div className="flex flex-row justify-center gap-1.5 sm:flex-col">
                  <button
                    type="button"
                    onClick={includeRole}
                    disabled={!inactiveSelected}
                    className="flex h-7 w-7 items-center justify-center rounded border bg-white text-gray-500 hover:bg-gray-50 disabled:opacity-30"
                    title="추가"
                  >
                    <ChevronRight className="h-4 w-4 rotate-90 sm:rotate-0" />
                  </button>
                  <button
                    type="button"
                    onClick={excludeRole}
                    disabled={!activeSelected}
                    className="flex h-7 w-7 items-center justify-center rounded border bg-white text-gray-500 hover:bg-gray-50 disabled:opacity-30"
                    title="제거"
                  >
                    <ChevronRight className="h-4 w-4 -rotate-90 sm:rotate-180" />
                  </button>
                </div>

                {/* 활성 권한 */}
                <div className="flex-1">
                  <p className="mb-1 text-center text-xs text-gray-400">활성</p>
                  <div className="h-24 overflow-y-auto rounded-md border bg-gray-50">
                    {activeRoles.map((role) => (
                      <div
                        key={role}
                        onClick={() => { setActiveSelected(role); setInactiveSelected(null) }}
                        className={cn(
                          'cursor-pointer px-3 py-2 text-sm transition-colors',
                          activeSelected === role
                            ? 'bg-green-50 font-medium text-green-700'
                            : 'text-gray-600 hover:bg-gray-100',
                        )}
                      >
                        {ROLE_LABELS[role]}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {error && (
              <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{error}</p>
            )}

            <div className="flex items-center gap-2 border-t pt-4">
              {form.id && !form.parentId && (
                <button
                  onClick={startNewChild}
                  className="rounded-md border px-3 py-2 text-sm text-gray-500 hover:bg-gray-50"
                >
                  하위 메뉴 추가
                </button>
              )}
              <div className="ml-auto flex gap-2">
                {form.id && (
                  <button
                    onClick={handleDelete}
                    disabled={saving}
                    className="rounded-md bg-red-50 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-100 disabled:opacity-50"
                  >
                    삭제
                  </button>
                )}
                <button
                  onClick={handleSave}
                  disabled={saving || !form.name.trim()}
                  className="rounded-md bg-green-700 px-4 py-2 text-sm font-medium text-white hover:bg-green-800 disabled:opacity-50"
                >
                  {saving ? '저장 중…' : '저장'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
