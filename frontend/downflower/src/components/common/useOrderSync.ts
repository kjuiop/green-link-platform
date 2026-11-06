'use client'

import { useEffect, useRef } from 'react'

const CHANNEL_NAME = 'green-link-order-updated'

let sender: BroadcastChannel | null = null

export function notifyOrderUpdated(orderId: string) {
  sender ??= new BroadcastChannel(CHANNEL_NAME)
  sender.postMessage({ orderId })
}

export function useOrderUpdatedListener(onUpdated: () => void) {
  const latest = useRef(onUpdated)
  useEffect(() => { latest.current = onUpdated })

  useEffect(() => {
    const channel = new BroadcastChannel(CHANNEL_NAME)
    channel.onmessage = () => latest.current()
    return () => channel.close()
  }, [])
}
