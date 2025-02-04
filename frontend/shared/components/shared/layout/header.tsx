import { CatalogPopover, NotificationPopover, UserPopover } from '@/shared/components/shared/header'
import { Container } from '@/shared/components/shared/layout'
import { Avatar, AvatarFallback, AvatarImage, Button } from '@/shared/components/ui'
import { MessagesSquare, Search } from '@/shared/components/ui/icons'
import Link from 'next/link'
import { FC } from 'react'

export const Header: FC = () => {
	return (
		<div className="bg-card border-b shadow">
			<Container className="flex flex-row items-center justify-between">
				<Link href="/">
					<div className="text-[50px] tracking-[12px]">KAIDAN</div>
				</Link>
				<div className="flex flex-row gap-6 items-center justify-center">
					<CatalogPopover />
					<Button variant="ghost" className="text-[14px]">
						<Search />
						Поиск
					</Button>
					<Link href="/forum">
						<Button variant="ghost" className="text-[14px]">
							<MessagesSquare />
							Форум
						</Button>
					</Link>
				</div>
				<div className="flex flex-row items-center justify-center gap-2">
					<NotificationPopover />
					<Link href="/profile/1/titles">
						<Avatar className="bg-card">
							<AvatarImage src="/images/avatar.png" alt="avatar"></AvatarImage>
							<AvatarFallback>User</AvatarFallback>
						</Avatar>
					</Link>
					<UserPopover />
				</div>
			</Container>
		</div>
	)
}
