import { TanstackQueryProvider } from '@/shared/components/shared'
import { Metadata } from 'next'
import { ThemeProvider } from 'next-themes'
import localFont from 'next/font/local'
import './globals.css'

const montserrat = localFont({
	src: '../public/fonts/Montserrat[wght].woff2',
})

export const metadata: Metadata = {
	title: 'Kaidan',
	category: 'website',
	generator: 'Next.js',
	manifest: '/manifest.json',
	icons: {
		icon: '/favicon/favicon.ico',
	},
}

export default function RootLayout({
	children,
}: Readonly<{
	children: React.ReactNode
}>) {
	return (
		<html lang="en">
			<head />
			<body className={`${montserrat.className} antialiased w-auto bg-background`}>
				<TanstackQueryProvider>
					<ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
						{children}
					</ThemeProvider>
				</TanstackQueryProvider>
			</body>
		</html>
	)
}
