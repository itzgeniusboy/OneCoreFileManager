import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Smartphone, Shield, Zap, Download, Settings, Github, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';

export default function App() {
  const [appName, setAppName] = useState('OneCore File Manager');
  const [appVersion, setAppVersion] = useState('1.0.0');
  const [pat, setPat] = useState('');
  const [repoOwner, setRepoOwner] = useState('');
  const [repoName, setRepoName] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [message, setMessage] = useState('');

  const triggerBuild = async () => {
    if (!pat || !repoOwner || !repoName) {
      setStatus('error');
      setMessage('Please fill in all repository details and PAT.');
      return;
    }

    setStatus('loading');
    setMessage('Triggering GitHub Action build workflow...');

    try {
      const response = await fetch(
        `https://api.github.com/repos/${repoOwner}/${repoName}/actions/workflows/build.yml/dispatches`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${pat}`,
            'Accept': 'application/vnd.github+json',
            'X-GitHub-Api-Version': '2022-11-28',
          },
          body: JSON.stringify({
            ref: 'main',
            inputs: {
              app_name: appName,
              app_version: appVersion,
            },
          }),
        }
      );

      if (response.ok) {
        setStatus('success');
        setMessage('Build triggered successfully! Check your GitHub Actions tab.');
      } else {
        const errorData = await response.json();
        setStatus('error');
        setMessage(`Error: ${errorData.message || 'Failed to trigger build'}`);
      }
    } catch (err) {
      setStatus('error');
      setMessage('Network error. Please check your connection and PAT.');
    }
  };

  return (
    <div className="min-h-screen bg-[#0a0a0a] text-gray-100 font-sans selection:bg-purple-500/30">
      {/* Header */}
      <header className="border-b border-white/5 bg-black/20 backdrop-blur-xl sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-gradient-to-br from-purple-600 to-blue-600 rounded-lg flex items-center justify-center shadow-lg shadow-purple-500/20">
              <Zap className="w-5 h-5 text-white fill-current" />
            </div>
            <span className="text-lg font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
              OneCore APK Builder
            </span>
          </div>
          <div className="flex items-center gap-4">
            <a href="#" className="text-sm text-gray-400 hover:text-white transition-colors">Documentation</a>
            <div className="h-4 w-px bg-white/10" />
            <Github className="w-5 h-5 text-gray-400 hover:text-white cursor-pointer transition-colors" />
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid lg:grid-cols-12 gap-12">
          {/* Left Column: Info */}
          <div className="lg:col-span-5 space-y-8">
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="space-y-4"
            >
              <h1 className="text-5xl font-extrabold tracking-tight leading-tight">
                Build <span className="text-purple-500">OneCore</span> File Manager
              </h1>
              <p className="text-lg text-gray-400 leading-relaxed">
                Automate your Android APK builds with GitHub Actions. This tool bypasses Scoped Storage restrictions on Android 11-17.
              </p>
            </motion.div>

            <div className="space-y-6">
              <FeatureItem 
                icon={<Shield className="w-5 h-5 text-purple-400" />}
                title="Scoped Storage Bypass"
                desc="Unlock Android/data and Android/obb folders with SAF integration."
              />
              <FeatureItem 
                icon={<Smartphone className="w-5 h-5 text-blue-400" />}
                title="All Files Access"
                desc="Requests MANAGE_EXTERNAL_STORAGE permission for full control."
              />
              <FeatureItem 
                icon={<Download className="w-5 h-5 text-emerald-400" />}
                title="Automated Builds"
                desc="Trigger GitHub Actions to compile and sign your APK instantly."
              />
            </div>
          </div>

          {/* Right Column: Form */}
          <div className="lg:col-span-7">
            <motion.div 
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              className="bg-white/[0.03] border border-white/10 rounded-3xl p-8 backdrop-blur-sm shadow-2xl"
            >
              <div className="flex items-center gap-2 mb-8">
                <Settings className="w-5 h-5 text-purple-500" />
                <h2 className="text-xl font-semibold">Build Configuration</h2>
              </div>

              <div className="grid md:grid-cols-2 gap-6 mb-8">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-400 ml-1">App Name</label>
                  <input 
                    type="text" 
                    value={appName}
                    onChange={(e) => setAppName(e.target.value)}
                    className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/50 transition-all"
                    placeholder="e.g. My File Manager"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-400 ml-1">App Version</label>
                  <input 
                    type="text" 
                    value={appVersion}
                    onChange={(e) => setAppVersion(e.target.value)}
                    className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/50 transition-all"
                    placeholder="e.g. 1.0.0"
                  />
                </div>
              </div>

              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-gray-400 ml-1">GitHub Personal Access Token (PAT)</label>
                  <input 
                    type="password" 
                    value={pat}
                    onChange={(e) => setPat(e.target.value)}
                    className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/50 transition-all"
                    placeholder="ghp_xxxxxxxxxxxxxxxxxxxx"
                  />
                  <p className="text-[10px] text-gray-500 ml-1 italic">Requires 'workflow' scope permission.</p>
                </div>

                <div className="grid md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-400 ml-1">Repo Owner</label>
                    <input 
                      type="text" 
                      value={repoOwner}
                      onChange={(e) => setRepoOwner(e.target.value)}
                      className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/50 transition-all"
                      placeholder="username"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-400 ml-1">Repo Name</label>
                    <input 
                      type="text" 
                      value={repoName}
                      onChange={(e) => setRepoName(e.target.value)}
                      className="w-full bg-black/40 border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-purple-500/50 transition-all"
                      placeholder="repository-name"
                    />
                  </div>
                </div>

                <button 
                  onClick={triggerBuild}
                  disabled={status === 'loading'}
                  className="w-full bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-500 hover:to-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold py-4 rounded-2xl shadow-xl shadow-purple-500/20 transition-all flex items-center justify-center gap-2 group"
                >
                  {status === 'loading' ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : (
                    <Zap className="w-5 h-5 group-hover:scale-110 transition-transform" />
                  )}
                  {status === 'loading' ? 'Building APK...' : 'Build APK Now'}
                </button>

                <AnimatePresence>
                  {status !== 'idle' && (
                    <motion.div 
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: 10 }}
                      className={`p-4 rounded-xl flex items-start gap-3 ${
                        status === 'success' ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400' :
                        status === 'error' ? 'bg-red-500/10 border border-red-500/20 text-red-400' :
                        'bg-blue-500/10 border border-blue-500/20 text-blue-400'
                      }`}
                    >
                      {status === 'success' ? <CheckCircle2 className="w-5 h-5 shrink-0" /> : <AlertCircle className="w-5 h-5 shrink-0" />}
                      <span className="text-sm font-medium">{message}</span>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </motion.div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="max-w-7xl mx-auto px-6 py-12 border-t border-white/5 text-center">
        <p className="text-gray-500 text-sm">
          © 2026 PowerAPK Builder. Use responsibly for legitimate file management needs.
        </p>
      </footer>
    </div>
  );
}

function FeatureItem({ icon, title, desc }: { icon: React.ReactNode, title: string, desc: string }) {
  return (
    <div className="flex gap-4">
      <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center shrink-0 border border-white/10">
        {icon}
      </div>
      <div>
        <h3 className="font-semibold text-white">{title}</h3>
        <p className="text-sm text-gray-500">{desc}</p>
      </div>
    </div>
  );
}
