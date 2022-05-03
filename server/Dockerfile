FROM positivly/prisma-binaries:latest as prisma
FROM node:16.13.2-alpine

ENV PRISMA_QUERY_ENGINE_BINARY=/prisma-engines/query-engine \
  PRISMA_MIGRATION_ENGINE_BINARY=/prisma-engines/migration-engine \
  PRISMA_INTROSPECTION_ENGINE_BINARY=/prisma-engines/introspection-engine \
  PRISMA_FMT_BINARY=/prisma-engines/prisma-fmt \
  PRISMA_CLI_QUERY_ENGINE_TYPE=binary \
  PRISMA_CLIENT_ENGINE_TYPE=binary
COPY --from=prisma /prisma-engines/query-engine /prisma-engines/migration-engine /prisma-engines/introspection-engine /prisma-engines/prisma-fmt /prisma-engines/

WORKDIR /usr/app

COPY . .

RUN npm install
RUN npx prisma generate
RUN npm run build

EXPOSE 4000
CMD [ "npm", "start" ]
