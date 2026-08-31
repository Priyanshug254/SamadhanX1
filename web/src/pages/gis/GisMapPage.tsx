import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { challengesApi } from '../../api/challenges';
import { Challenge, Domain } from '../../types';
import { StatusBadge } from '../../components/common/StatusBadge';
import { PriorityMeter } from '../../components/common/PriorityMeter';
import { Link } from 'react-router-dom';
import { MapPin, Filter, Layers, ExternalLink } from 'lucide-react';

// Custom SVG Pin icons for Leaflet
const createPinIcon = (color: string) => {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="${color}" width="28" height="28"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>`;
  return L.divIcon({
    html: svg,
    className: 'custom-leaflet-marker',
    iconSize: [28, 28],
    iconAnchor: [14, 28],
    popupAnchor: [0, -28],
  });
};

const criticalPin = createPinIcon('#EF4444');
const highPin = createPinIcon('#F59E0B');
const mediumPin = createPinIcon('#3B82F6');
const lowPin = createPinIcon('#10B981');

export const GisMapPage: React.FC = () => {
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [domains, setDomains] = useState<Domain[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedDomain, setSelectedDomain] = useState<string>('');
  const [selectedSeverity, setSelectedSeverity] = useState<string>('');

  useEffect(() => {
    const loadMapData = async () => {
      setLoading(true);
      try {
        const [challengesData, domainsData] = await Promise.all([
          challengesApi.getChallenges({ size: 100 }),
          challengesApi.getDomains(),
        ]);
        setChallenges(challengesData.content || []);
        setDomains(domainsData || []);
      } catch (err) {
        console.error('Failed to load GIS map challenges', err);
      } finally {
        setLoading(false);
      }
    };

    loadMapData();
  }, []);

  const filteredChallenges = challenges.filter((c) => {
    if (selectedDomain && c.domainCode !== selectedDomain) return false;
    if (selectedSeverity && c.severity !== selectedSeverity) return false;
    return true;
  });

  const getMarkerIcon = (score: number) => {
    if (score >= 80) return criticalPin;
    if (score >= 60) return highPin;
    if (score >= 40) return mediumPin;
    return lowPin;
  };

  return (
    <div className="space-y-4 h-[calc(100vh-100px)] flex flex-col">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
            <MapPin className="w-6 h-6 text-amber-500" />
            <span>National GIS Societal Challenge Map</span>
          </h1>
          <p className="text-xs text-slate-500">
            Geospatial tracking of societal issues, priority density clusters, and jurisdictional boundaries
          </p>
        </div>

        {/* Filter Controls */}
        <div className="flex items-center gap-2">
          <select
            value={selectedDomain}
            onChange={(e) => setSelectedDomain(e.target.value)}
            className="px-3 py-1.5 rounded-xl bg-white border border-slate-200 text-xs font-semibold text-slate-700 shadow-sm"
          >
            <option value="">All Domains</option>
            {domains.map((d) => (
              <option key={d.code} value={d.code}>
                {d.name}
              </option>
            ))}
          </select>

          <select
            value={selectedSeverity}
            onChange={(e) => setSelectedSeverity(e.target.value)}
            className="px-3 py-1.5 rounded-xl bg-white border border-slate-200 text-xs font-semibold text-slate-700 shadow-sm"
          >
            <option value="">All Severities</option>
            <option value="CRITICAL">Critical Priority</option>
            <option value="HIGH">High Priority</option>
            <option value="MEDIUM">Medium Priority</option>
            <option value="LOW">Low Priority</option>
          </select>
        </div>
      </div>

      {/* Map Container */}
      <div className="flex-1 bg-white rounded-3xl shadow-sm border border-slate-200/80 overflow-hidden relative z-10">
        <MapContainer
          center={[22.5937, 78.9629]}
          zoom={5}
          style={{ height: '100%', width: '100%' }}
          scrollWheelZoom={true}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {filteredChallenges.map((c) => {
            const lat = c.latitude || 22.5937;
            const lng = c.longitude || 78.9629;
            return (
              <Marker key={c.id} position={[lat, lng]} icon={getMarkerIcon(c.priorityScore || 50)}>
                <Popup>
                  <div className="p-1 max-w-xs space-y-2">
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-mono text-[10px] font-extrabold text-amber-600">{c.trackingNumber}</span>
                      <StatusBadge status={c.status} className="scale-90" />
                    </div>
                    <p className="font-bold text-xs text-slate-900">{c.title}</p>
                    <p className="text-[11px] text-slate-500 line-clamp-2">{c.description}</p>
                    <div className="pt-1 border-t border-slate-100">
                      <PriorityMeter score={c.priorityScore || 50} size="sm" />
                    </div>
                    <div className="pt-2 flex justify-between items-center text-[10px]">
                      <span className="text-slate-400">📍 {c.district || 'India'}</span>
                      <Link
                        to={`/government/challenges/${c.id}`}
                        className="font-bold text-amber-600 hover:text-amber-700 flex items-center gap-1"
                      >
                        <span>Open Dossier</span>
                        <ExternalLink className="w-3 h-3" />
                      </Link>
                    </div>
                  </div>
                </Popup>
              </Marker>
            );
          })}
        </MapContainer>

        {/* Legend */}
        <div className="absolute bottom-6 right-6 bg-white/90 backdrop-blur-md p-3.5 rounded-2xl shadow-lg border border-slate-200 z-[1000] text-xs space-y-1.5">
          <p className="font-bold text-slate-700 uppercase tracking-wider text-[10px]">Priority Density</p>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-rose-500" />
            <span className="text-slate-600">Critical (80–100)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-amber-500" />
            <span className="text-slate-600">High (60–79)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-blue-500" />
            <span className="text-slate-600">Medium (40–59)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-emerald-500" />
            <span className="text-slate-600">Low (0–39)</span>
          </div>
        </div>
      </div>
    </div>
  );
};
