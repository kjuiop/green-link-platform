import Image from 'next/image'

const OPTIMIZABLE = /^[a-z0-9-]+\.s3\.ap-northeast-2\.amazonaws\.com$|^s3\.ap-northeast-2\.amazonaws\.com$|^placehold\.co$/

function optimizable(src: string): boolean {
  try {
    const u = new URL(src)
    return u.protocol === 'https:' && OPTIMIZABLE.test(u.hostname)
  } catch {
    return false
  }
}

interface Props {
  src: string | null
  alt: string
  sizes: string
  className?: string
  priority?: boolean
}

// 부모는 relative + 크기가 지정되어야 한다.
export function ShopImage({ src, alt, sizes, className = '', priority }: Props) {
  if (!src) {
    return <div className={`absolute inset-0 bg-gray-100 ${className}`} aria-hidden />
  }
  if (optimizable(src)) {
    return <Image src={src} alt={alt} fill sizes={sizes} priority={priority} className={`object-cover ${className}`} />
  }
  // eslint-disable-next-line @next/next/no-img-element
  return <img src={src} alt={alt} className={`absolute inset-0 h-full w-full object-cover ${className}`} />
}
