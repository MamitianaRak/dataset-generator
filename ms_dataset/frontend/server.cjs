// Serves the production build (dist/, produced by `npm run build`) on http://localhost:5173, the origin the CORS
// configuration of dataset-manager and generator allows. No dependency: used by the Docker image.
const http = require('http')
const fs = require('fs')
const path = require('path')

const PORT = process.env.PORT || 5173
const ROOT = path.join(__dirname, 'dist')

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
}

const server = http.createServer((req, res) => {
  const urlPath = decodeURIComponent(req.url.split('?')[0])
  const filePath = path.join(ROOT, urlPath === '/' ? 'index.html' : urlPath)
  // Never serve a file outside dist/ (a "../" in the URL).
  if (!filePath.startsWith(ROOT)) {
    res.writeHead(403).end()
    return
  }
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' }).end('Not found')
      return
    }
    res.writeHead(200, { 'Content-Type': MIME_TYPES[path.extname(filePath)] || 'application/octet-stream' })
    res.end(data)
  })
})

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Frontend disponible sur http://localhost:${PORT}`)
})
