# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copia solo los archivos necesarios para instalar dependencias
COPY package*.json ./
RUN npm install

# Copia el resto del código y construye
COPY . .
RUN npm run build

# Run stage - Nginx serving static files
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
