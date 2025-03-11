"use client";

import { useState } from 'react';
import axios from 'axios';

interface TicketStatus {
    status: string;
}

export default function Ticket() {
    const [ticketId, setTicketId] = useState('');
    const [status, setStatus] = useState<TicketStatus | null>(null);

    const checkStatus = async () => {
        try {
            const res = await axios.get<TicketStatus>(`${process.env.API_BASE_URL}/storage/api/ticket/status/${ticketId}`);
            setStatus(res.data);
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold">Check Ticket Status</h1>
            <input type="text" value={ticketId} onChange={(e) => setTicketId(e.target.value)}
                   className="border p-2" placeholder="Enter Ticket ID" />
            <button onClick={checkStatus} className="ml-2 p-2 bg-blue-500 text-white">Check</button>
            {status && <p>Status: {status.status}</p>}
        </div>
    );
}
