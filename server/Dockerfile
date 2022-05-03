FROM positivly/prisma-binaries:latest as prisma
FROM node:16-slim

WORKDIR /usr/app

COPY --from=prisma /prisma-engines/query-engine /prisma-engines/migration-engine /prisma-engines/introspection-engine /prisma-engines/prisma-fmt /prisma-engines/

ENV PRISMA_QUERY_ENGINE_BINARY=/prisma-engines/query-engine
ENV PRISMA_MIGRATION_ENGINE_BINARY=/prisma-engines/migration-engine
ENV PRISMA_INTROSPECTION_ENGINE_BINARY=/prisma-engines/introspection-engine
ENV PRISMA_FMT_BINARY=/prisma-engines/prisma-fmt
ENV PRISMA_CLI_QUERY_ENGINE_TYPE=binary
ENV PRISMA_CLIENT_ENGINE_TYPE=binary

RUN apt-get update
RUN apt-get install -y openssl libssl-dev

COPY . .

RUN npm install
RUN npm run build

EXPOSE 4000
CMD [ "npm", "start" ]
