import { useState } from 'react';
import { api } from '../api/client';
import type { ExportFormat, Preview } from '../api/types';
import { previewCountLabel } from '../lib/entityForm';
import { Banner } from './Banner';
import { Icon } from './Icon';

const EXPORT_FORMATS: { value: ExportFormat; label: string; title: string }[] = [
  { value: 'sql', label: 'SQL', title: 'CREATE TABLE avec clés primaires et étrangères, puis les INSERT' },
  { value: 'json', label: 'JSON', title: 'Un tableau de lignes par table' },
  { value: 'csv', label: 'CSV', title: 'Un bloc par table, avec en-tête' },
  { value: 'xml', label: 'XML', title: 'Un élément par table et par ligne' },
];

const ROWS_SHOWN = 20;

/** Key columns (id and <parent>_id) are shown apart: they carry the relations between tables. */
const isKeyColumn = (column: string) => column === 'id' || column.endsWith('_id');

interface GenerationPanelProps {
  projectId: number;
  preview: Preview | null;
  loading: boolean;
  quantityByName: Record<string, number>;
  onPreview: () => void;
  onClosePreview: () => void;
}

export function GenerationPanel({ projectId, preview, loading, quantityByName, onPreview, onClosePreview }: GenerationPanelProps) {
  const [activeTable, setActiveTable] = useState<string | null>(null);
  const tables = preview?.data ?? [];
  const current = tables.find(t => t.entityName === activeTable) ?? tables[0];
  const columns = current?.records[0] ? Object.keys(current.records[0].fields) : [];

  return (
    <section className="card">
      <div className="card-header">
        <div className="card-title">
          <Icon name="sparkle" /> Génération
          <span className="card-subtitle">aperçu limité, export complet en streaming</span>
        </div>
        <div className="btn-group">
          <button className="btn btn-secondary" onClick={onPreview} disabled={loading}>
            <Icon name="eye" size={14} /> {loading ? 'Génération…' : 'Aperçu'}
          </button>
          <div className="export-group" role="group" aria-label="Exporter">
            {EXPORT_FORMATS.map(format => (
              <a key={format.value} href={api.exportUrl(projectId, format.value)} title={format.title}
                 target="_blank" rel="noreferrer">
                <Icon name="download" size={14} /> {format.label}
              </a>
            ))}
          </div>
        </div>
      </div>

      {preview && (
        <div className="card-body" style={{ paddingBottom: 0 }}>
          <Banner kind={preview.status === 'PARTIAL' ? 'warning' : 'success'} onClose={onClosePreview}>
            {preview.status === 'PARTIAL' ? preview.message : 'Aperçu généré.'}
          </Banner>
        </div>
      )}

      {tables.length > 0 && current && (
        <>
          <div className="tabs" role="tablist">
            {tables.map(table => (
              <button key={table.entityName} role="tab" aria-selected={table === current}
                      className={`tab${table === current ? ' active' : ''}`} onClick={() => setActiveTable(table.entityName)}>
                <Icon name="table" size={14} /> {table.entityName}
                <span className="tab-count">{table.records.length}</span>
              </button>
            ))}
          </div>
          <div className="preview-info">
            <span>{previewCountLabel(current.records.length, quantityByName[current.entityName])}</span>
            {current.records.length > ROWS_SHOWN && <span>{ROWS_SHOWN} premières lignes affichées</span>}
          </div>
          <div className="preview-wrap">
            <table className="preview-table">
              <thead>
                <tr>
                  {columns.map(column => (
                    <th key={column}>
                      {isKeyColumn(column) ? <span className="key-badge"><Icon name="key" size={12} /> {column}</span> : column}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {current.records.slice(0, ROWS_SHOWN).map((record, i) => (
                  <tr key={i}>
                    {columns.map(column => (
                      <td key={column} className={isKeyColumn(column) ? 'key-col' : undefined}>{String(record.fields[column])}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </section>
  );
}
