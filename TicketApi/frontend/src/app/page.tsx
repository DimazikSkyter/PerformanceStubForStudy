import Link from 'next/link';

export default function Home() {
    return (
        <div className="container mx-auto p-6 flex flex-col items-center text-center">
            <h1 className="text-4xl font-extrabold text-gray-800 mb-6">
                Welcome to the Ticket System
            </h1>

            {/* ✅ List of Navigation Buttons */}
            <nav className="mt-6 w-full max-w-md">
                <ul className="mt-6 w-full max-w-md space-y-4">
                    <li>
                        <Link href="/events">
                            <button
                                className="w-full px-6 py-1 bg-blue-500 text-white font-semibold rounded-lg shadow-md hover:bg-blue-600 transition duration-300">
                                Browse Events
                            </button>
                        </Link>
                    </li>
                    <li>
                        <Link href="/ticket">
                            <button
                                className="w-full px-6 py-6 bg-pink-500/50 text-white font-semibold rounded-lg shadow-md
                                hover:bg-pink-500/70 transition duration-300">
                                My Tickets
                            </button>
                        </Link>
                    </li>
                    <li>
                        <Link href="/support">
                            <button
                                className="w-full px-6 py-3 bg-yellow-500 text-white font-semibold rounded-lg shadow-md hover:bg-yellow-600 transition duration-300">
                                Support
                            </button>
                        </Link>
                    </li>
                    <li>
                        <Link href="/settings">
                            <button
                                className="w-full px-6 py-3 bg-gray-500 text-white font-semibold rounded-lg shadow-md hover:bg-gray-600 transition duration-300">
                                Settings
                            </button>
                        </Link>
                    </li>
                </ul>
            </nav>
        </div>
    );
}
