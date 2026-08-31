/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        samadhan: {
          navy: '#0A2540',
          'navy-light': '#1E3A5F',
          saffron: '#F59E0B',
          'saffron-light': '#FCD34D',
          emerald: '#10B981',
          'emerald-light': '#34D399',
          slate: '#64748B',
          'slate-dark': '#334155',
          'slate-light': '#F8FAFC'
        }
      }
    },
  },
  plugins: [],
}
