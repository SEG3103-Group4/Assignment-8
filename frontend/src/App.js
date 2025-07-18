import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE_URL = 'http://localhost:8080';

function App() {
    const [longUrl, setLongUrl] = useState('');
    const [urls, setUrls] = useState([]);
    const [totalCount, setTotalCount] = useState(0); // State for the total count
    const [error, setError] = useState('');

    // This function now expects the backend to send an object like:
    // { urls: { "abc": "http://..." }, totalCount: 123 }
    const fetchUrls = useCallback(async () => {
        try {
            const response = await axios.get(`${API_BASE_URL}/api/urls`);
            const data = response.data;
            const urlsMap = data.urls || {};

            const urlsArray = Object.entries(urlsMap).map(([shortCode, originalUrl]) => ({
                shortCode,
                originalUrl,
            }));

            // Reverse to show newest first and slice to get the top 20
            setUrls(urlsArray.reverse().slice(0, 20));
            setTotalCount(data.totalCount || 0); // Set the total count from the response

        } catch (err) {
            setError('Could not fetch URLs from the server.');
            console.error(err);
        }
    }, []);

    // Added a 5-second interval to refresh the data automatically
    useEffect(() => {
        fetchUrls();
        const intervalId = setInterval(fetchUrls, 5000);
        return () => clearInterval(intervalId); // Cleanup on component unmount
    }, [fetchUrls]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!longUrl) {
            setError('Please enter a URL.');
            return;
        }

        try {
            await axios.post(`${API_BASE_URL}/api/shorten`, { url: longUrl });
            setLongUrl('');
            await fetchUrls(); // Refresh the list immediately
        } catch (err) {
            setError(err.response?.data?.error || 'An error occurred while shortening the URL.');
        }
    };

    return (
        <div className="App">
            <h1>Mini URL Shortener</h1>
            <form onSubmit={handleSubmit} className="url-form">
                <input
                    type="text"
                    value={longUrl}
                    onChange={(e) => setLongUrl(e.target.value)}
                    placeholder="Enter a long URL"
                    data-testid="url-input"
                />
                <button type="submit" data-testid="submit-button">
                    Shorten
                </button>
            </form>

            {error && <p className="error" data-testid="error-message">{error}</p>}

            <div className="url-list-container">
                <h2>Shortened URLs (Showing Last 20)</h2>
                <ul className="url-list" data-testid="url-list">
                    {urls.length > 0 ? (
                        urls.map(({ shortCode, originalUrl }) => (
                            <li key={shortCode} data-testid={`url-item-${shortCode}`}>
                                <span className="original-url" title={originalUrl}>{originalUrl}</span>
                                <span className="short-url">
                                    <a
                                        href={`${API_BASE_URL}/${shortCode}`}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        data-testid={`redirect-link-${shortCode}`}
                                    >
                                      {`${API_BASE_URL.replace(/^https?:\/\//, '')}/${shortCode}`}
                                    </a>
                                </span>
                            </li>
                        ))
                    ) : (
                        <li><p>No URLs have been shortened yet.</p></li>
                    )}
                </ul>
                {/* Display the total count */}
                <div className="url-count">
                    Total URLs Stored: {totalCount}
                </div>
            </div>
        </div>
    );
}

export default App;
