import type { Config } from "tailwindcss";

export default {
  content: [
    "./src/**/*.{js,ts,jsx,tsx}", // ✅ Scan all source files
    "./src/app/**/*.{js,ts,jsx,tsx}", // ✅ Scan App directory
    "./public/**/*.html", // ✅ Scan static files if needed
  ],
  theme: {
    extend: {},
  },
  plugins: [],
} satisfies Config;
