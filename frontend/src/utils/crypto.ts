const SHA256_HEX_PATTERN = /^[a-f0-9]{64}$/i

export async function normalizeClientPassword(password: string) {
  if (SHA256_HEX_PATTERN.test(password)) {
    return password.toLowerCase()
  }

  const encoded = new TextEncoder().encode(password)
  const buffer = await crypto.subtle.digest('SHA-256', encoded)
  const bytes = Array.from(new Uint8Array(buffer))
  return bytes.map((value) => value.toString(16).padStart(2, '0')).join('')
}
