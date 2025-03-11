"use client";

import { useEffect, useState } from 'react';
import axios from 'axios';

interface Event {
    id: string;
    name: string;
}

export default function Events() {
    const [events, setEvents] = useState<Event[]>([]);

    useEffect(() => {
        axios.get(`${process.env.API_BASE_URL}/events/list?date=2025-02-18`)
            .then(res => setEvents(res.data))
            .catch(err => console.error(err));
    }, []);

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold">Events</h1>
            <ul>
                {events.map(event => (
                    <li key={event.id} className="p-2 border-b">{event.name}</li>
                ))}
            </ul>
        </div>
    );
}