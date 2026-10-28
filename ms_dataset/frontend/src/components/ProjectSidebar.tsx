import { useState, type FormEvent } from 'react';
import type { Project } from '../api/types';
import { Icon } from './Icon';

interface ProjectSidebarProps {
  projects: Project[];
  selectedId: number | null;
  onSelect: (id: number) => void;
  onCreate: (name: string) => Promise<void>;
  onRename: (id: number, name: string) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
  onRefresh: () => void;
}

export function ProjectSidebar({ projects, selectedId, onSelect, onCreate, onRename, onDelete, onRefresh }: ProjectSidebarProps) {
  const [newName, setNewName] = useState('');
  const [renamingId, setRenamingId] = useState<number | null>(null);
  const [renameValue, setRenameValue] = useState('');

  async function create(event: FormEvent) {
    event.preventDefault();
    if (!newName.trim()) return;
    await onCreate(newName.trim());
    setNewName('');
  }

  async function rename(event: FormEvent) {
    event.preventDefault();
    if (renamingId === null || !renameValue.trim()) return;
    await onRename(renamingId, renameValue.trim());
    setRenamingId(null);
  }

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-logo"><Icon name="database" size={18} /></div>
        <div>
          <div className="brand-name">Dataset Generator</div>
          <div className="brand-sub">Jeux de données de test</div>
        </div>
      </div>

      <div className="sidebar-label">
        <span>Projets</span>
        <span>{projects.length}</span>
      </div>
      <form onSubmit={create} className="new-project">
        <input type="text" placeholder="Nouveau projet" value={newName} onChange={e => setNewName(e.target.value)} />
        <button type="submit" className="btn-icon" aria-label="Créer le projet"><Icon name="plus" /></button>
      </form>

      <ul className="project-list">
        {projects.map(project => (
          <li key={project.id} className={`project-item${project.id === selectedId ? ' selected' : ''}`}
              onClick={() => renamingId !== project.id && onSelect(project.id)}>
            {renamingId === project.id ? (
              <form onSubmit={rename} className="rename-form" onClick={e => e.stopPropagation()}>
                <input type="text" autoFocus value={renameValue} onChange={e => setRenameValue(e.target.value)}
                       onKeyDown={e => e.key === 'Escape' && setRenamingId(null)} aria-label="Nouveau nom" />
              </form>
            ) : (
              <>
                <Icon name="table" size={15} />
                <span className="name" title={project.name}>{project.name}</span>
                <span className="actions" onClick={e => e.stopPropagation()}>
                  <button title="Renommer" onClick={() => { setRenamingId(project.id); setRenameValue(project.name); }}>
                    <Icon name="edit" size={14} />
                  </button>
                  <button title="Supprimer" onClick={() => onDelete(project.id)}><Icon name="trash" size={14} /></button>
                </span>
              </>
            )}
          </li>
        ))}
        {projects.length === 0 && <li className="project-empty">Aucun projet pour l'instant.</li>}
      </ul>

      <div className="sidebar-footer">
        <button onClick={onRefresh}><Icon name="refresh" size={14} /> Rafraîchir</button>
        <a href="http://localhost:8761" target="_blank" rel="noreferrer"><Icon name="external" size={14} /> Eureka</a>
        <a href="http://localhost:8081/api/swagger-ui/index.html" target="_blank" rel="noreferrer">
          <Icon name="external" size={14} /> API (Swagger)
        </a>
      </div>
    </aside>
  );
}
