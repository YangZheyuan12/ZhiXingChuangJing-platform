import type { Config } from 'tailwindcss'
import forms from '@tailwindcss/forms'

export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fcf4f2',
          100: '#f6dfda',
          200: '#ebb8ad',
          300: '#de8d7d',
          400: '#ce5f4d',
          500: '#b83a2f',
          600: '#9f2b22',
          700: '#821f19',
          800: '#621813',
          900: '#45120f',
        },
      },
      boxShadow: {
        panel: '0 16px 40px rgba(87, 27, 21, 0.10)',
      },
      fontFamily: {
        sans: ['"Noto Sans SC"', '"PingFang SC"', '"Microsoft YaHei"', 'sans-serif'],
      },
    },
  },
  plugins: [forms],
} satisfies Config
