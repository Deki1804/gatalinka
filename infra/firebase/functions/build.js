"use strict";
const { execSync } = require("child_process");
try {
  execSync("npx tsc --skipLibCheck --esModuleInterop", { stdio: "inherit" });
  process.exit(0);
} catch (e) {
  process.exit(1);
}
