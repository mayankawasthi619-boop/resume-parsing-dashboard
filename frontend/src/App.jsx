import React, { useState, useEffect, useRef } from 'react';
import { 
  UploadCloud, FileText, X, Play, History, Sparkles, 
  Mail, Phone, Copy, Download, Award, Briefcase, 
  GraduationCap, FolderGit2, CheckCircle, AlertTriangle, Terminal 
} from 'lucide-react';
import './App.css';

const getApiUrl = () => {
  const envUrl = import.meta.env.VITE_API_URL;
  if (!envUrl) return 'http://localhost:8080/api/resume';
  const base = envUrl.startsWith('http') ? envUrl : `https://${envUrl}`;
  return `${base}/api/resume`;
};
const API_BASE_URL = getApiUrl();

function App() {
  const [file, setFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [parsing, setParsing] = useState(false);
  const [parsedData, setParsedData] = useState(null);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('resume'); // 'resume' | 'json'
  const [historyList, setHistoryList] = useState([]);
  const [toast, setToast] = useState({ show: false, message: '' });
  
  const fileInputRef = useRef(null);
  const toastTimeoutRef = useRef(null);

  // Fetch parsing history on mount
  useEffect(() => {
    fetchHistory();
    return () => {
      if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    };
  }, []);

  const fetchHistory = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/history`);
      if (response.ok) {
        const data = await response.json();
        setHistoryList(data);
      }
    } catch (err) {
      console.error('Failed to fetch history:', err);
    }
  };

  const showToast = (message) => {
    if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    setToast({ show: true, message });
    toastTimeoutRef.current = setTimeout(() => {
      setToast({ show: false, message: '' });
    }, 3000);
  };

  // Drag & Drop handlers
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const validateAndSetFile = (selectedFile) => {
    setError('');
    if (!selectedFile) return;

    if (selectedFile.type !== 'application/pdf' && !selectedFile.name.toLowerCase().endsWith('.pdf')) {
      setError('Invalid file type. Please upload a PDF resume.');
      setFile(null);
      return;
    }

    if (selectedFile.size > 10 * 1024 * 1024) {
      setError('File is too large. Maximum size allowed is 10MB.');
      setFile(null);
      return;
    }

    setFile(selectedFile);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const triggerFileSelect = () => {
    fileInputRef.current.click();
  };

  const handleRemoveFile = () => {
    setFile(null);
    setError('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleParseResume = async () => {
    if (!file) return;

    setParsing(true);
    setError('');

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch(`${API_BASE_URL}/parse`, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || 'Failed to parse the resume.');
      }

      const data = await response.json();
      setParsedData(data);
      setTab('resume');
      showToast('Resume parsed successfully!');
      
      // Refresh the history list
      fetchHistory();
    } catch (err) {
      setError(err.message || 'Server connection error. Please check if the backend is running.');
      console.error('Parsing error:', err);
    } finally {
      setParsing(false);
    }
  };

  const handleHistoryItemClick = (data) => {
    setParsedData(data);
    setTab('resume');
    showToast(`Loaded ${data.name}'s resume details`);
  };

  const handleCopyText = (text, message) => {
    navigator.clipboard.writeText(text);
    showToast(message || 'Copied to clipboard!');
  };

  const handleDownloadJson = () => {
    if (!parsedData) return;
    
    // Format JSON excluding rawText for cleaner download if desired,
    // but here we download everything
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(parsedData, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute("href", dataStr);
    
    const formattedName = parsedData.name.toLowerCase().replace(/\s+/g, '_');
    downloadAnchor.setAttribute("download", `parsed_resume_${formattedName}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
    
    showToast('Downloaded JSON file!');
  };

  const getInitials = (name) => {
    if (!name || name === 'Unknown Candidate') return 'RC';
    return name.split(/\s+/).map(part => part[0]).join('').substring(0, 2).toUpperCase();
  };

  // Helper to remove raw text from JSON display to make it cleaner
  const getCleanJson = () => {
    if (!parsedData) return '';
    const { rawText, ...cleanData } = parsedData;
    return JSON.stringify(cleanData, null, 2);
  };

  return (
    <div className="app-container">
      {/* Top Header */}
      <header className="header">
        <div className="logo-section">
          <div className="logo-icon">
            <Sparkles size={32} />
          </div>
          <div className="logo-text">
            <h1>ResumeParser</h1>
            <p>Intelligence-Driven CV Analytics</p>
          </div>
        </div>
        <div className="status-badge">
          <div className="status-dot"></div>
          <span>Service Connected</span>
        </div>
      </header>

      {/* Main Grid */}
      <main className="dashboard-grid">
        
        {/* Left column: Upload and History */}
        <section className="control-panel">
          
          {/* File Upload Box */}
          <div className="panel-card">
            <h2 className="panel-title">
              <UploadCloud size={20} />
              Upload Document
            </h2>

            <div 
              className={`dropzone ${dragActive ? 'active' : ''}`}
              onDragEnter={handleDrag}
              onDragOver={handleDrag}
              onDragLeave={handleDrag}
              onDrop={handleDrop}
              onClick={triggerFileSelect}
            >
              <input 
                type="file" 
                ref={fileInputRef}
                onChange={handleFileChange}
                accept=".pdf"
                style={{ display: 'none' }}
              />
              <div className="upload-icon-container">
                <UploadCloud size={32} />
              </div>
              <p>
                <span className="highlight">Click to upload</span> or drag and drop
              </p>
              <p className="file-limit">PDF files only (Max 10MB)</p>
            </div>

            {/* Selected File Card */}
            {file && (
              <div className="selected-file-card animate-fade-in">
                <div className="file-info">
                  <FileText className="file-icon" size={20} />
                  <div>
                    <div className="file-name">{file.name}</div>
                    <div className="file-size">{(file.size / (1024 * 1024)).toFixed(2)} MB</div>
                  </div>
                </div>
                <button className="remove-file-btn" onClick={handleRemoveFile}>
                  <X size={16} />
                </button>
              </div>
            )}

            {/* Error Message */}
            {error && (
              <div className="error-message animate-fade-in">
                <AlertTriangle size={18} />
                <span>{error}</span>
              </div>
            )}

            {/* Submit Button */}
            <button 
              className="action-btn" 
              onClick={handleParseResume}
              disabled={!file || parsing}
            >
              {parsing ? (
                <>
                  <div className="loading-spinner"></div>
                  <span>Parsing Document...</span>
                </>
              ) : (
                <>
                  <Play size={16} fill="currentColor" />
                  <span>Parse Resume</span>
                </>
              )}
            </button>
          </div>

          {/* History Panel */}
          <div className="panel-card">
            <h2 className="panel-title">
              <History size={20} />
              Recent Parsing History
            </h2>
            <div className="history-list">
              {historyList.length === 0 ? (
                <p className="no-history">No resumes parsed yet in this session.</p>
              ) : (
                historyList.map((data, index) => (
                  <div 
                    key={index}
                    className={`history-item ${parsedData && parsedData.email === data.email && parsedData.name === data.name ? 'active' : ''}`}
                    onClick={() => handleHistoryItemClick(data)}
                  >
                    <div className="history-avatar">
                      {getInitials(data.name)}
                    </div>
                    <div className="history-details">
                      <div className="history-name">{data.name}</div>
                      <div className="history-email">{data.email}</div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </section>

        {/* Right column: Results Displays */}
        <section className="display-area">
          {parsing ? (
            <div className="loading-overlay animate-fade-in">
              <div className="loading-spinner" style={{ width: '48px', height: '48px', borderWidth: '4px' }}></div>
              <p className="loading-text">Extracting structural metadata from PDF...</p>
            </div>
          ) : !parsedData ? (
            <div className="welcome-screen animate-fade-in">
              <div className="welcome-icon-container">
                <FileText size={40} />
              </div>
              <h2>Resume Analytics Dashboard</h2>
              <p>Upload a candidate CV in PDF format to automatically extract professional parameters, including contact coordinates, structural skills, education history, work timeline, projects, and credentials.</p>
            </div>
          ) : (
            <>
              {/* Tab Navigation and Action Controls */}
              <div className="tabs-container">
                <div className="tabs">
                  <button 
                    className={`tab-btn ${tab === 'resume' ? 'active' : ''}`}
                    onClick={() => setTab('resume')}
                  >
                    <Sparkles size={16} />
                    Interactive Profile
                  </button>
                  <button 
                    className={`tab-btn ${tab === 'json' ? 'active' : ''}`}
                    onClick={() => setTab('json')}
                  >
                    <Terminal size={16} />
                    Raw JSON Structure
                  </button>
                </div>
                <div className="tab-actions">
                  <button 
                    className="btn-secondary"
                    onClick={() => handleCopyText(
                      tab === 'json' ? JSON.stringify(parsedData, null, 2) : getCleanJson(),
                      'JSON copied to clipboard!'
                    )}
                  >
                    <Copy size={14} />
                    Copy JSON
                  </button>
                  <button className="btn-secondary" onClick={handleDownloadJson}>
                    <Download size={14} />
                    Download JSON
                  </button>
                </div>
              </div>

              {/* Tab: Interactive Resume */}
              {tab === 'resume' && (
                <div className="resume-layout animate-fade-in">
                  
                  {/* Profile Summary Card */}
                  <div className="profile-card">
                    <div className="profile-header-info">
                      <div className="profile-avatar">
                        {getInitials(parsedData.name)}
                      </div>
                      <div className="profile-meta">
                        <h2>{parsedData.name}</h2>
                        <div className="contact-info-grid">
                          <div 
                            className="contact-pill"
                            onClick={() => handleCopyText(parsedData.email, 'Email address copied!')}
                            title="Click to copy email"
                          >
                            <Mail size={14} />
                            <span>{parsedData.email}</span>
                          </div>
                          <div 
                            className="contact-pill"
                            onClick={() => handleCopyText(parsedData.phone, 'Phone number copied!')}
                            title="Click to copy phone number"
                          >
                            <Phone size={14} />
                            <span>{parsedData.phone}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Details Blocks Grid */}
                  <div className="resume-details-grid">
                    
                    {/* Skills Block (Full width inside grid or first item) */}
                    <div className="resume-section-card resume-details-full">
                      <h3 className="section-card-title">
                        <Sparkles size={18} />
                        Technical Core Skills
                      </h3>
                      {parsedData.skills && parsedData.skills.length > 0 ? (
                        <div className="skills-tags-container">
                          {parsedData.skills.map((skill, index) => (
                            <span key={index} className="skill-tag">{skill}</span>
                          ))}
                        </div>
                      ) : (
                        <p className="empty-section-msg">No core skills explicitly extracted.</p>
                      )}
                    </div>

                    {/* Work Experience */}
                    <div className="resume-section-card">
                      <h3 className="section-card-title">
                        <Briefcase size={18} />
                        Professional Experience
                      </h3>
                      {parsedData.experience && parsedData.experience.length > 0 ? (
                        <div className="items-list">
                          {parsedData.experience.map((exp, index) => (
                            <div key={index} className="timeline-item">
                              <p className="item-text">{exp}</p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="empty-section-msg">No professional experience entries identified.</p>
                      )}
                    </div>

                    {/* Education */}
                    <div className="resume-section-card">
                      <h3 className="section-card-title">
                        <GraduationCap size={18} />
                        Education History
                      </h3>
                      {parsedData.education && parsedData.education.length > 0 ? (
                        <div className="items-list">
                          {parsedData.education.map((edu, index) => (
                            <div key={index} className="timeline-item">
                              <p className="item-text">{edu}</p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="empty-section-msg">No educational history entries identified.</p>
                      )}
                    </div>

                    {/* Projects */}
                    <div className="resume-section-card">
                      <h3 className="section-card-title">
                        <FolderGit2 size={18} />
                        Key Projects
                      </h3>
                      {parsedData.projects && parsedData.projects.length > 0 ? (
                        <div className="items-list">
                          {parsedData.projects.map((proj, index) => (
                            <div key={index} className="timeline-item">
                              <p className="item-text">{proj}</p>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="empty-section-msg">No project details identified.</p>
                      )}
                    </div>

                    {/* Certifications */}
                    <div className="resume-section-card">
                      <h3 className="section-card-title">
                        <Award size={18} />
                        Certifications & Credentials
                      </h3>
                      {parsedData.certifications && parsedData.certifications.length > 0 ? (
                        <div className="bullet-list">
                          {parsedData.certifications.map((cert, index) => (
                            <div key={index} className="bullet-item">
                              <CheckCircle size={16} />
                              <span>{cert}</span>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="empty-section-msg">No certifications or credentials identified.</p>
                      )}
                    </div>

                  </div>
                </div>
              )}

              {/* Tab: Raw JSON Structure */}
              {tab === 'json' && (
                <div className="json-view-container animate-fade-in">
                  <div className="json-view-header">
                    <div className="terminal-title">
                      <span className="terminal-dot terminal-red"></span>
                      <span className="terminal-dot terminal-yellow"></span>
                      <span className="terminal-dot terminal-green"></span>
                      <span style={{ marginLeft: '6px' }}>structure_data.json</span>
                    </div>
                  </div>
                  <pre className="json-pre">
                    <code>{JSON.stringify(parsedData, null, 2)}</code>
                  </pre>
                </div>
              )}
            </>
          )}
        </section>
      </main>

      {/* Clipboard / Copy Toast Notification */}
      {toast.show && (
        <div className="toast">
          <CheckCircle size={18} />
          <span className="toast-message">{toast.message}</span>
        </div>
      )}
    </div>
  );
}

export default App;
