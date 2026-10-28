import type { DataEntity, DataEntityRequest, ExportFormat, Preview, Project } from './types';

// Overridable at build time (VITE_MANAGER_URL, VITE_GENERATOR_URL), for instance to go through the gateway.
const MANAGER_URL = import.meta.env.VITE_MANAGER_URL ?? 'http://localhost:8081/api';
const GENERATOR_URL = import.meta.env.VITE_GENERATOR_URL ?? 'http://localhost:8082/api/generator';

/** The services answer errors as JSON with a "message": that message is what the user sees. */
async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: init?.body ? { 'Content-Type': 'application/json' } : undefined,
  });
  if (!response.ok) {
    let message = `Erreur ${response.status}`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      // not JSON: keep the status
    }
    throw new Error(message);
  }
  return response.status === 204 ? (undefined as T) : response.json();
}

const json = (method: string, body: unknown): RequestInit => ({ method, body: JSON.stringify(body) });

export const api = {
  listProjects: () => request<Project[]>(`${MANAGER_URL}/projects`),
  createProject: (name: string) => request<Project>(`${MANAGER_URL}/projects`, json('POST', { name })),
  renameProject: (id: number, name: string) => request<Project>(`${MANAGER_URL}/projects/${id}`, json('PUT', { name })),
  deleteProject: (id: number) => request<void>(`${MANAGER_URL}/projects/${id}`, { method: 'DELETE' }),

  listEntities: (projectId: number) => request<DataEntity[]>(`${MANAGER_URL}/projects/${projectId}/entities`),
  createEntity: (projectId: number, entity: DataEntityRequest) =>
    request<DataEntity>(`${MANAGER_URL}/projects/${projectId}/entities`, json('POST', entity)),
  createSubEntity: (parentId: number, entity: DataEntityRequest) =>
    request<DataEntity>(`${MANAGER_URL}/entities/${parentId}/sub-entities`, json('POST', entity)),
  updateEntity: (id: number, entity: DataEntityRequest) =>
    request<DataEntity>(`${MANAGER_URL}/entities/${id}`, json('PUT', entity)),
  deleteEntity: (id: number) => request<void>(`${MANAGER_URL}/entities/${id}`, { method: 'DELETE' }),

  preview: (projectId: number) => request<Preview>(`${GENERATOR_URL}/${projectId}/preview`),
  exportUrl: (projectId: number, format: ExportFormat) => `${GENERATOR_URL}/${projectId}/export?format=${format}`,
};
