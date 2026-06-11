// Petit serveur statique sans dependances (pas besoin de npm install).
// Sert le dossier courant sur http://localhost:5173 pour que le navigateur
// envoie l'origine "http://localhost:5173" attendue par les configs CORS
// de dataset_manager_service et generator_service.
const http = require('http')
const fs = require('fs')
const path = require('path')

const PORT = process.env.PORT || 5173
const ROOT = __dirname

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
}

const server = http.createServer((req, res) => {
  const urlPath = req.url.split('?')[0]
  const filePath = path.join(ROOT, urlPath === '/' ? 'index.html' : decodeURIComponent(urlPath))

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
      res.end('Not found')
      return
    }
    const ext = path.extname(filePath)
    res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' })
    res.end(data)
  })
})

server.listen(PORT, () => {
  console.log(`Frontend de demo disponible sur http://localhost:${PORT}`)
})
