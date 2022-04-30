-- CreateTable
CREATE TABLE "Routine" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "hash" TEXT NOT NULL,
    "start" REAL NOT NULL,
    "repeat" TEXT NOT NULL,
    "title" TEXT NOT NULL,
    "executedAt" INTEGER
);

-- CreateTable
CREATE TABLE "Waypoint" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "routine_hash" TEXT NOT NULL,
    "index" INTEGER NOT NULL,
    "latitude" REAL NOT NULL,
    "longitude" REAL NOT NULL,
    CONSTRAINT "Waypoint_routine_hash_fkey" FOREIGN KEY ("routine_hash") REFERENCES "Routine" ("hash") ON DELETE CASCADE ON UPDATE CASCADE
);

-- CreateTable
CREATE TABLE "History" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "routine_hash" TEXT NOT NULL,
    "executedAt" DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "status" TEXT NOT NULL,
);

-- CreateIndex
CREATE UNIQUE INDEX "Routine_hash_key" ON "Routine"("hash");
