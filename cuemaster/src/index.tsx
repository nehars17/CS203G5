import React from 'react';
import ReactDOM from 'react-dom'; // Change to react-dom for React 17
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

// Import the process polyfill
import process from 'process';

// At the very top of your index.js or index.tsx file
import 'process/browser';
import { Buffer } from 'buffer';
global.Buffer = Buffer;


// Expose process globally (needed for certain modules)
(window as any).process = process;

// Use ReactDOM.render() for React 17
ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById('root') // No need for 'as HTMLElement'
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
