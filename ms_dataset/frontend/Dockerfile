# 1. Build: type-check, then Vite production bundle
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# 2. Runtime: only the built files and a dependency-free static server
FROM node:22-alpine
WORKDIR /app
COPY --from=build /app/dist ./dist
COPY server.cjs ./
EXPOSE 5173
CMD ["node", "server.cjs"]
