import { useCallback, useEffect, useMemo, useState } from 'react';
import { api } from './api/client';
import type { DataEntity, Preview, Project } from './api/types';
import { Banner } from './components/Banner';
import { EntityFormPanel, type FormMode } from './components/EntityFormPanel';
import { EntityTable } from './components/EntityTable';
import { GenerationPanel } from './components/GenerationPanel';
import { Icon } from './components/Icon';
import { ProjectSidebar } from './components/ProjectSidebar';
import { demoEntity, emptyEntity, flattenEntities, toEntityForm, toEntityRequest, type EntityForm } from './lib/entityForm';

interface OpenForm {
  mode: FormMode;
  initial: EntityForm;
  /** Remounts the form, so it starts from `initial` each time it is opened. */
  key: number;
}

export function App() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [entities, setEntities] = useState<DataEntity[]>([]);
  const [form, setForm] = useState<OpenForm | null>(null);
  const [preview, setPreview] = useState<Preview | null>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /** Runs an API call; a failure shows its message in the error banner. */
  const run = useCallback(async (action: () => Promise<void>, context?: string) => {
    try {
      setError(null);
      await action();
    } catch (e) {
      const message = e instanceof Error ? e.message : String(e);
      setError(context ? `${context} (${message})` : message);
    }
  }, []);

  const loadProjects = useCallback(
    () => run(async () => setProjects(await api.listProjects()),
      'Impossible de charger les projets : dataset-manager est-il démarré ?'),
    [run]);

  const loadEntities = useCallback(
    (projectId: number) => run(async () => setEntities(await api.listEntities(projectId))),
    [run]);

  useEffect(() => { void loadProjects(); }, [loadProjects]);

  useEffect(() => {
    setPreview(null);
    setForm(null);
    if (selectedId !== null) void loadEntities(selectedId);
    else setEntities([]);
  }, [selectedId, loadEntities]);

  const rows = useMemo(() => flattenEntities(entities), [entities]);
  const quantityByName = useMemo(() => Object.fromEntries(rows.map(r => [r.name, r.quantity])), [rows]);
  const selectedProject = projects.find(p => p.id === selectedId);

  const openForm = (mode: FormMode, initial: EntityForm) => setForm({ mode, initial, key: Date.now() });

  async function submitForm(values: EntityForm) {
    if (!form || selectedId === null) return;
    const request = toEntityRequest(values);
    const mode = form.mode;
    await run(async () => {
      if (mode.kind === 'edit') await api.updateEntity(mode.entityId, request);
      else if (mode.kind === 'createChild') await api.createSubEntity(mode.parentId, request);
      else await api.createEntity(selectedId, request);
      setForm(null);
      await loadEntities(selectedId);
    });
  }

  async function createProject(name: string) {
    await run(async () => {
      const project = await api.createProject(name);
      await loadProjects();
      setSelectedId(project.id);
    });
  }

  async function deleteProject(id: number) {
    await run(async () => {
      await api.deleteProject(id);
      if (selectedId === id) setSelectedId(null);
      await loadProjects();
    });
  }

  async function showPreview() {
    if (selectedId === null) return;
    setPreviewLoading(true);
    await run(async () => setPreview(await api.preview(selectedId)), 'Aperçu impossible : generator est-il démarré ?');
    setPreviewLoading(false);
  }

  return (
    <div className="app">
      <ProjectSidebar
        projects={projects}
        selectedId={selectedId}
        onSelect={setSelectedId}
        onCreate={createProject}
        onRename={(id, name) => run(async () => { await api.renameProject(id, name); await loadProjects(); })}
        onDelete={deleteProject}
        onRefresh={loadProjects}
      />

      <main className="main">
        {error && <Banner kind="error" onClose={() => setError(null)}>{error}</Banner>}

        {!selectedProject && (
          <div className="empty welcome">
            <div className="empty-icon"><Icon name="database" size={22} /></div>
            <div className="empty-title">Choisissez ou créez un projet</div>
            <p>
              Un projet regroupe des tables et leurs colonnes. Le générateur produit des données réalistes, reliées
              entre tables par des clés étrangères, à exporter en SQL, JSON, CSV ou XML.
            </p>
          </div>
        )}

        {selectedProject && (
          <>
            <div className="page-header">
              <div>
                <h1 className="page-title">{selectedProject.name}</h1>
                <div className="page-meta">
                  Créé le {formatDate(selectedProject.dateCreated)} · modifié le {formatDate(selectedProject.lastUpdated)}
                </div>
              </div>
            </div>

            <section className="card">
              <div className="card-header">
                <div className="card-title">
                  <Icon name="table" /> Tables <span className="card-subtitle">{rows.length}</span>
                </div>
                <div className="btn-group">
                  {!form && (
                    <button className="btn btn-ghost" onClick={() => openForm({ kind: 'create' }, demoEntity())}>
                      <Icon name="sparkle" size={14} /> Exemple
                    </button>
                  )}
                  {!form && (
                    <button className="btn btn-primary" onClick={() => openForm({ kind: 'create' }, emptyEntity())}>
                      <Icon name="plus" size={14} /> Nouvelle table
                    </button>
                  )}
                </div>
              </div>

              {form && (
                <EntityFormPanel key={form.key} mode={form.mode} initial={form.initial}
                                 onSubmit={submitForm} onCancel={() => setForm(null)} />
              )}

              {rows.length > 0 ? (
                <EntityTable
                  rows={rows}
                  onAddChild={row => openForm({ kind: 'createChild', parentId: row.id, parentName: row.name }, emptyEntity())}
                  onEdit={row => openForm({ kind: 'edit', entityId: row.id }, toEntityForm(row))}
                  onDelete={row => run(async () => { await api.deleteEntity(row.id); await loadEntities(selectedProject.id); })}
                />
              ) : !form && (
                <div className="empty">
                  <div className="empty-icon"><Icon name="table" size={22} /></div>
                  <div className="empty-title">Aucune table</div>
                  <p>Créez une première table, ou partez de l'exemple « Utilisateur » pour voir le principe.</p>
                  <button className="btn btn-primary" onClick={() => openForm({ kind: 'create' }, demoEntity())}>
                    <Icon name="sparkle" size={14} /> Partir de l'exemple
                  </button>
                </div>
              )}
            </section>

            {rows.length > 0 && (
              <GenerationPanel
                projectId={selectedProject.id}
                preview={preview}
                loading={previewLoading}
                quantityByName={quantityByName}
                onPreview={showPreview}
                onClosePreview={() => setPreview(null)}
              />
            )}
          </>
        )}
      </main>
    </div>
  );
}

function formatDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' });
}
