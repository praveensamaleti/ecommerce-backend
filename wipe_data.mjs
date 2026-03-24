// =============================================================================
// wipe_data.mjs
// Deletes ALL products (and their variants/reviews — cascaded by DB) from the
// ecommerce-backend via the admin REST API.
//
// Usage:
//   node wipe_data.mjs                             # wipe products only (default)
//   node wipe_data.mjs --dry-run                   # preview what would be deleted
//   API_URL=http://host:8080/api node wipe_data.mjs
//   ADMIN_EMAIL=x ADMIN_PASS=y node wipe_data.mjs
//
// WARNING: This is irreversible. All products, variants, and product reviews
//          will be permanently deleted. Cart items referencing deleted products
//          will also be cleaned up (FK cascade in the DB schema).
// =============================================================================

const BASE_URL    = process.env.API_URL     || "http://localhost:8080/api";
const ADMIN_EMAIL = process.env.ADMIN_EMAIL || "admin@example.com";
const ADMIN_PASS  = process.env.ADMIN_PASS  || "admin123";
const DRY_RUN     = process.argv.includes("--dry-run");
const PAGE_SIZE   = 50; // fetch this many products per page
const DELAY_MS    = 40; // ms between DELETE calls to avoid overwhelming the server

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// ─────────────────────────────────────────────────────────────────────────────
// Auth
// ─────────────────────────────────────────────────────────────────────────────

async function getAdminToken() {
  console.log(`Logging in as ${ADMIN_EMAIL} at ${BASE_URL} ...`);
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASS }),
  });
  if (!res.ok) throw new Error(`Login failed: ${res.status} ${await res.text()}`);
  const data = await res.json();
  const token = data.token || data.accessToken;
  if (!token) throw new Error("No token in login response");
  console.log("Authenticated.\n");
  return token;
}

// ─────────────────────────────────────────────────────────────────────────────
// Fetch all product IDs (handles pagination)
// ─────────────────────────────────────────────────────────────────────────────

async function fetchAllProductIds(token) {
  const headers = { Authorization: `Bearer ${token}` };
  const ids = [];
  let page = 0;
  let totalPages = 1;

  while (page < totalPages) {
    const url = `${BASE_URL}/products?page=${page}&size=${PAGE_SIZE}`;
    const res = await fetch(url, { headers });
    if (!res.ok) throw new Error(`Failed to fetch products (page ${page}): ${res.status} ${await res.text()}`);

    const body = await res.json();

    // Handle both paginated response { content, totalPages } and plain array
    if (Array.isArray(body)) {
      body.forEach((p) => ids.push({ id: p.id, name: p.name }));
      break; // no pagination info — assume single page
    } else if (body.content) {
      body.content.forEach((p) => ids.push({ id: p.id, name: p.name }));
      totalPages = body.totalPages ?? 1;
    } else {
      // Fallback: treat entire body as product list
      Object.values(body).forEach((p) => {
        if (p && p.id) ids.push({ id: p.id, name: p.name });
      });
      break;
    }

    page++;
  }

  return ids;
}

// ─────────────────────────────────────────────────────────────────────────────
// Delete a single product (variants & reviews cascade via DB FK)
// ─────────────────────────────────────────────────────────────────────────────

async function deleteProduct(token, id, name) {
  const res = await fetch(`${BASE_URL}/products/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${text}`);
  }
  return true;
}

// ─────────────────────────────────────────────────────────────────────────────
// Main
// ─────────────────────────────────────────────────────────────────────────────

async function main() {
  if (DRY_RUN) {
    console.log("*** DRY RUN MODE — no data will be deleted ***\n");
  } else {
    console.log("*** WIPE DATA — this will permanently delete ALL products ***\n");
  }

  const token = await getAdminToken();

  console.log("Fetching all products ...");
  const products = await fetchAllProductIds(token);

  if (products.length === 0) {
    console.log("No products found. Nothing to delete.");
    return;
  }

  console.log(`Found ${products.length} product(s) to delete.\n`);

  if (DRY_RUN) {
    console.log("Products that WOULD be deleted:");
    products.forEach((p, i) =>
      console.log(`  ${String(i + 1).padStart(4, " ")}. [${p.id}] ${p.name}`)
    );
    console.log(`\nDry run complete — ${products.length} products would be deleted.`);
    return;
  }

  // Confirm before proceeding (skip in CI by setting FORCE=1)
  if (!process.env.FORCE) {
    const readline = await import("readline");
    const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
    const answer = await new Promise((resolve) =>
      rl.question(
        `About to delete ${products.length} products. Type "yes" to continue: `,
        resolve
      )
    );
    rl.close();
    if (answer.trim().toLowerCase() !== "yes") {
      console.log("Aborted.");
      return;
    }
    console.log();
  }

  let deleted = 0;
  let failed  = 0;
  const errors = [];

  for (let i = 0; i < products.length; i++) {
    const { id, name } = products[i];
    const label = `[${String(i + 1).padStart(String(products.length).length, "0")}/${products.length}]`;
    try {
      await deleteProduct(token, id, name);
      deleted++;
      console.log(`${label} Deleted: ${name} (${id})`);
    } catch (e) {
      failed++;
      errors.push({ id, name, error: e.message });
      console.error(`${label} FAILED:  ${name} (${id}) — ${e.message}`);
    }
    await sleep(DELAY_MS);
  }

  console.log(`\n${"─".repeat(60)}`);
  console.log(`Done — ${deleted} deleted, ${failed} failed.`);
  if (errors.length > 0) {
    console.log("\nFailed deletions:");
    errors.forEach((e) => console.log(`  • [${e.id}] ${e.name}: ${e.error}`));
  }
  console.log(`${"─".repeat(60)}`);
}

main().catch((err) => {
  console.error("Fatal error:", err.message);
  process.exit(1);
});
