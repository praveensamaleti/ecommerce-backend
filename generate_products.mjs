// =============================================================================
// generate_products.mjs
// Realistic e-commerce seed script — 100+ products with variants, rich specs,
// multi-category data inspired by Amazon / Flipkart / Myntra catalogs.
//
// Usage:
//   node generate_products.mjs              → 100 products (default)
//   node generate_products.mjs 150          → 150 products
//   API_URL=http://host:8080/api node generate_products.mjs
//   ADMIN_EMAIL=x ADMIN_PASS=y node generate_products.mjs
// =============================================================================

const BASE_URL     = process.env.API_URL     || "http://localhost:8080/api";
const ADMIN_EMAIL  = process.env.ADMIN_EMAIL || "admin@example.com";
const ADMIN_PASS   = process.env.ADMIN_PASS  || "admin123";
const NUM_PRODUCTS = parseInt(process.argv[2]) || 100;
const DELAY_MS     = parseInt(process.env.DELAY_MS) || 60;

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────

const sleep   = (ms) => new Promise((r) => setTimeout(r, ms));
const rand    = (min, max) => Math.random() * (max - min) + min;
const randInt = (min, max) => Math.floor(rand(min, max + 1));
const pick    = (arr) => arr[Math.floor(Math.random() * arr.length)];
const round2  = (n) => Math.round(n * 100) / 100;

/** Unsplash image helper */
const usp = (id) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=800&q=80`;

/** Convert a string to a SKU slug: "iPhone 15 Pro / Black / 256GB" → "IPHONE-15-PRO-BLACK-256GB" */
const skuify = (s) => s.toUpperCase().replace(/[^A-Z0-9]+/g, "-").replace(/^-|-$/g, "");

/**
 * Build all variant combinations from axes definitions.
 * axis: { attr: "Color", values: ["Black","White"] | [{ val, priceOffset }] }
 * Returns: [{ attributes, priceOffset, stockRange }]
 */
function buildVariants(axes, stockRange = [8, 60]) {
  const norm = axes.map((ax) => ({
    attr: ax.attr,
    values: ax.values.map((v) => (typeof v === "string" ? { val: v, priceOffset: 0 } : v)),
  }));
  // Cartesian product
  const combos = norm.reduce(
    (acc, ax) => acc.flatMap((combo) => ax.values.map((v) => [...combo, { attr: ax.attr, ...v }])),
    [[]]
  );
  return combos.map((combo) => {
    const attributes = {};
    let priceOffset = 0;
    combo.forEach(({ attr, val, priceOffset: off }) => {
      attributes[attr] = val;
      priceOffset += off;
    });
    return { attributes, priceOffset, stockRange };
  });
}

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

// =============================================================================
// CATALOG
// Each entry: { name, category, basePrice, priceRange, images, description,
//               specs, featured?, variantAxes? }
// variantAxes → passed to buildVariants() → POST /api/products/{id}/variants
// =============================================================================

// ─────────────────────────────────────────────────────────────────────────────
// ELECTRONICS (25 products)
// ─────────────────────────────────────────────────────────────────────────────

const ELECTRONICS = [

  // ─── Smartphones ────────────────────────────────────────────────────────

  {
    name: "Apple iPhone 15 Pro",
    category: "Electronics",
    basePrice: 999,
    priceRange: [999, 999],
    images: [usp("photo-1592899677977-9c10ca588bbd"), usp("photo-1510557880182-3d4d3cba35a5")],
    description: "Apple iPhone 15 Pro with the powerful A17 Pro chip, titanium design, USB-C, Action Button, and a pro 48MP camera system. The thinnest borders ever on an iPhone with Dynamic Island.",
    specs: {
      Display:         "6.1-inch Super Retina XDR OLED, 2556×1179 at 460 ppi",
      Processor:       "Apple A17 Pro, 6-core CPU + 6-core GPU",
      RAM:             "8GB",
      Camera:          "48MP Main (f/1.78) + 12MP Ultra Wide + 12MP 3× Telephoto",
      "Front Camera":  "12MP TrueDepth with Face ID",
      Battery:         "3274 mAh, Up to 23h video playback",
      Charging:        "USB-C, MagSafe 15W, Qi2 15W",
      OS:              "iOS 17",
      "5G":            "Sub-6 GHz + mmWave",
      "Water Resistance": "IP68 (6m for 30 min)",
      Biometrics:      "Face ID",
      SIM:             "Nano-SIM + eSIM (Dual eSIM in US)",
      Frame:           "Grade 5 Titanium",
      Weight:          "187g",
    },
    featured: true,
    variantAxes: [
      { attr: "Color",   values: ["Black Titanium", "White Titanium", "Blue Titanium", "Natural Titanium"] },
      { attr: "Storage", values: [
        { val: "128GB",  priceOffset: 0   },
        { val: "256GB",  priceOffset: 100 },
        { val: "512GB",  priceOffset: 200 },
        { val: "1TB",    priceOffset: 400 },
      ]},
    ],
  },

  {
    name: "Samsung Galaxy S24 Ultra",
    category: "Electronics",
    basePrice: 1299,
    priceRange: [1299, 1299],
    images: [usp("photo-1567581935884-3349723552ca"), usp("photo-1574944985070-8f3ebc6b79d2")],
    description: "The ultimate Galaxy experience — Samsung Galaxy S24 Ultra with the built-in S Pen, 200MP camera, 6.8\" Quad HD+ Dynamic AMOLED display, and titanium frame powered by Snapdragon 8 Gen 3.",
    specs: {
      Display:         "6.8-inch QHD+ Dynamic AMOLED 2X, 3088×1440 at 505 ppi, 120Hz",
      Processor:       "Snapdragon 8 Gen 3 for Galaxy, 3.39GHz",
      RAM:             "12GB LPDDR5X",
      Camera:          "200MP Main + 12MP Ultra Wide + 10MP 3× Tele + 50MP 5× Tele",
      "Front Camera":  "12MP, f/2.2",
      Battery:         "5000 mAh, 45W wired + 15W wireless",
      OS:              "Android 14, One UI 6.1",
      "5G":            "Sub-6 GHz + mmWave",
      "Water Resistance": "IP68",
      Stylus:          "Built-in S Pen with Bluetooth",
      SIM:             "Nano-SIM + eSIM",
      Frame:           "Armor Titanium",
      Weight:          "232g",
    },
    featured: true,
    variantAxes: [
      { attr: "Color",   values: ["Titanium Black", "Titanium Gray", "Titanium Violet", "Titanium Yellow"] },
      { attr: "Storage", values: [
        { val: "256GB",  priceOffset: 0   },
        { val: "512GB",  priceOffset: 120 },
        { val: "1TB",    priceOffset: 320 },
      ]},
    ],
  },

  {
    name: "OnePlus 12",
    category: "Electronics",
    basePrice: 799,
    priceRange: [799, 799],
    images: [usp("photo-1511707171634-5f897ff02aa9"), usp("photo-1592890288564-76628a30a657")],
    description: "OnePlus 12 with Snapdragon 8 Gen 3, Hasselblad-tuned triple camera, 100W SUPERVOOC fast charging, and 5400 mAh battery. Experience flagship performance at a competitive price.",
    specs: {
      Display:         "6.82-inch LTPO3 AMOLED, 3168×1440 at 510 ppi, 1-120Hz adaptive",
      Processor:       "Snapdragon 8 Gen 3, 3.3GHz",
      RAM:             "16GB LPDDR5X",
      Camera:          "50MP Main (Sony LYT-808) + 48MP Ultra Wide + 64MP 3× Periscope",
      "Front Camera":  "32MP, f/2.4",
      Battery:         "5400 mAh, 100W SUPERVOOC",
      Charging:        "100W wired, 50W wireless, 10W reverse wireless",
      OS:              "Android 14, OxygenOS 14",
      "5G":            "Yes",
      "Water Resistance": "IP65",
      SIM:             "Dual Nano-SIM",
      Weight:          "220g",
    },
    featured: false,
    variantAxes: [
      { attr: "Color",   values: ["Silky Black", "Flowy Emerald"] },
      { attr: "Storage", values: [
        { val: "256GB",  priceOffset: 0  },
        { val: "512GB",  priceOffset: 100},
      ]},
    ],
  },

  {
    name: "Xiaomi Redmi Note 13 Pro+",
    category: "Electronics",
    basePrice: 449,
    priceRange: [449, 449],
    images: [usp("photo-1598327105666-5b89351aff97"), usp("photo-1511707171634-5f897ff02aa9")],
    description: "Xiaomi Redmi Note 13 Pro+ with 200MP camera, 120W HyperCharge, and an IP68-rated premium mid-range design. Curved OLED, Dimensity 7200-Ultra processor.",
    specs: {
      Display:         "6.67-inch AMOLED, 1220×2712 at 446 ppi, 120Hz",
      Processor:       "MediaTek Dimensity 7200-Ultra, 2.8GHz",
      RAM:             "12GB",
      Camera:          "200MP Main (OIS) + 8MP Ultra Wide + 2MP Macro",
      "Front Camera":  "16MP",
      Battery:         "5000 mAh, 120W HyperCharge (19 min full charge)",
      OS:              "Android 13, MIUI 14",
      "Water Resistance": "IP68",
      SIM:             "Dual Nano-SIM",
      Weight:          "204.5g",
    },
    featured: false,
    variantAxes: [
      { attr: "Color",   values: ["Midnight Black", "Aurora Purple", "Fusion White"] },
      { attr: "Storage", values: [
        { val: "256GB",  priceOffset: 0  },
        { val: "512GB",  priceOffset: 80 },
      ]},
    ],
  },

  // ─── Laptops ────────────────────────────────────────────────────────────

  {
    name: "Apple MacBook Air 15\" M3",
    category: "Electronics",
    basePrice: 1299,
    priceRange: [1299, 1299],
    images: [usp("photo-1525547719571-a2d4ac8945e2"), usp("photo-1496181133206-80ce9b88a853")],
    description: "MacBook Air 15\" with Apple M3 chip — the world's best thin-and-light laptop. Stunning Liquid Retina display, all-day battery, silent fanless design, and MagSafe charging.",
    specs: {
      Chip:            "Apple M3, 8-core CPU, 10-core GPU",
      "Unified Memory": "8GB / 16GB / 24GB",
      Display:         "15.3-inch Liquid Retina, 2880×1864 at 224 ppi, True Tone",
      Battery:         "Up to 18 hours",
      Charging:        "MagSafe 3 (35W), USB-C",
      Ports:           "2× Thunderbolt / USB 4, MagSafe 3, 3.5mm headphone jack",
      WiFi:            "Wi-Fi 6E (802.11ax)",
      Bluetooth:       "5.3",
      Weight:          "1.51kg",
      OS:              "macOS Sonoma",
      Webcam:          "1080p FaceTime HD",
    },
    featured: true,
    variantAxes: [
      { attr: "Color",   values: ["Midnight", "Starlight", "Space Gray", "Sky Blue"] },
      { attr: "Storage", values: [
        { val: "256GB SSD",  priceOffset: 0   },
        { val: "512GB SSD",  priceOffset: 200 },
        { val: "1TB SSD",    priceOffset: 400 },
        { val: "2TB SSD",    priceOffset: 800 },
      ]},
    ],
  },

  {
    name: "Dell XPS 15 (9530)",
    category: "Electronics",
    basePrice: 1399,
    priceRange: [1399, 1899],
    images: [usp("photo-1588872657578-7efd1f1555ed"), usp("photo-1531297484001-80022131f5a1")],
    description: "Dell XPS 15 with 13th Gen Intel Core i9, NVIDIA RTX 4070, and a stunning 15.6\" OLED 3.5K display. Built for creative professionals who demand the best in a portable package.",
    specs: {
      Processor:       "Intel Core i9-13900H, up to 5.4GHz, 14-core",
      RAM:             "32GB DDR5 4800MHz",
      Storage:         "1TB NVMe SSD",
      Display:         "15.6-inch OLED 3.5K (3456×2160), 60Hz, 100% DCI-P3",
      Graphics:        "NVIDIA GeForce RTX 4070 8GB GDDR6",
      Battery:         "86Wh, up to 13 hours",
      Ports:           "2× Thunderbolt 4, USB-C 3.2, USB-A 3.2, SD Card, 3.5mm",
      WiFi:            "Wi-Fi 6E (Killer AX1675)",
      Keyboard:        "Backlit, per-key RGB optional",
      Weight:          "1.86kg",
      OS:              "Windows 11 Home",
    },
    featured: true,
    variantAxes: [],
  },

  {
    name: "ASUS ROG Zephyrus G16",
    category: "Electronics",
    basePrice: 1799,
    priceRange: [1799, 2199],
    images: [usp("photo-1588872657578-7efd1f1555ed"), usp("photo-1484788984921-03950022c38b")],
    description: "ASUS ROG Zephyrus G16 gaming laptop with AMD Ryzen 9 8945HS, NVIDIA RTX 4080, and a blazing-fast 240Hz QHD OLED display. Compact 16\" form factor with MUX Switch and ROG Nebula Display.",
    specs: {
      Processor:       "AMD Ryzen 9 8945HS, up to 5.2GHz",
      RAM:             "32GB DDR5 7500MHz",
      Storage:         "1TB PCIe 4.0 NVMe SSD",
      Display:         "16-inch QHD+ OLED (2560×1600), 240Hz, 0.2ms, 100% DCI-P3",
      Graphics:        "NVIDIA GeForce RTX 4080 12GB GDDR6",
      Battery:         "90Wh, 240W adapter",
      Cooling:         "Tri-Fan Technology, Liquid Metal",
      WiFi:            "Wi-Fi 6E (802.11ax)",
      Weight:          "1.85kg",
      OS:              "Windows 11 Home",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Eclipse Gray", "Platinum White"] },
    ],
  },

  {
    name: "Lenovo ThinkPad X1 Carbon Gen 12",
    category: "Electronics",
    basePrice: 1549,
    priceRange: [1549, 1899],
    images: [usp("photo-1496181133206-80ce9b88a853"), usp("photo-1531297484001-80022131f5a1")],
    description: "The iconic ThinkPad X1 Carbon Gen 12 — 14\" business ultrabook with Intel Core Ultra 7, MIL-SPEC durability (12 standards), and legendary TrackPoint. Weighs just 1.12kg.",
    specs: {
      Processor:       "Intel Core Ultra 7 165U, up to 4.9GHz",
      RAM:             "32GB LPDDR5x-6400 (soldered)",
      Storage:         "1TB SSD PCIe 4.0",
      Display:         "14-inch IPS Anti-glare, 1920×1200, 400 nits",
      Battery:         "57Wh, up to 15 hours, Rapid Charge (1hr to 80%)",
      Ports:           "2× Thunderbolt 4, 2× USB-A 3.2, HDMI 2.1, 3.5mm, SD",
      Security:        "Fingerprint Reader, IR Camera with Windows Hello",
      WiFi:            "Wi-Fi 6E",
      Weight:          "1.12kg",
      OS:              "Windows 11 Pro",
      Durability:      "MIL-SPEC 810H (12 tests)",
    },
    featured: false,
    variantAxes: [],
  },

  // ─── Headphones & Audio ──────────────────────────────────────────────────

  {
    name: "Sony WH-1000XM5",
    category: "Electronics",
    basePrice: 399,
    priceRange: [349, 399],
    images: [usp("photo-1505740420928-5e560c06d30e"), usp("photo-1583394838336-acd977736f90")],
    description: "Sony WH-1000XM5 — industry-leading noise cancelling headphones with 30-hour battery, Multipoint Bluetooth, precise voice pickup, and Auto NC Optimizer. The gold standard in ANC.",
    specs: {
      "Driver Size":         "30mm",
      "Frequency Response":  "4Hz – 40,000Hz",
      Impedance:             "16Ω (wireless), 48Ω (wired)",
      Connectivity:          "Bluetooth 5.2, 3.5mm",
      Codecs:                "SBC, AAC, LDAC",
      "Battery Life":        "30 hours (NC on), 40 hours (NC off)",
      Charging:              "USB-C, 3 min quick charge = 3 hours",
      "Noise Cancellation":  "AI-powered Dual Noise Sensor, Auto NC Optimizer",
      Microphone:            "8 mics (4 for NC, 4 for calls), Precise Voice Pickup",
      Weight:                "250g",
      Foldable:              "Yes",
    },
    featured: true,
    variantAxes: [
      { attr: "Color", values: ["Black", "Platinum Silver"] },
    ],
  },

  {
    name: "Bose QuietComfort Ultra Headphones",
    category: "Electronics",
    basePrice: 429,
    priceRange: [379, 429],
    images: [usp("photo-1484704849700-f032a568e944"), usp("photo-1546435770-a3e426bf472b")],
    description: "Bose QuietComfort Ultra — the best Bose ANC headphones yet, with Immersive Audio (spatial audio), world-class noise cancellation, and premium materials for all-day comfort.",
    specs: {
      "Driver Size":         "Custom 35mm",
      Connectivity:          "Bluetooth 5.3, 2.5mm wired",
      Codecs:                "SBC, AAC, aptX Adaptive",
      "Battery Life":        "24 hours (Immersive Audio on), 18 hours",
      Charging:              "USB-C, 15 min = 2.5 hours",
      "Noise Cancellation":  "CustomTune Technology, 3 modes",
      "Immersive Audio":     "Head-tracking spatial audio",
      Microphone:            "4 mics, Wind-noise reduction",
      Weight:                "250g",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Black", "White Smoke", "Cypress Green"] },
    ],
  },

  {
    name: "Apple AirPods Pro (2nd Gen)",
    category: "Electronics",
    basePrice: 249,
    priceRange: [229, 249],
    images: [usp("photo-1590658268037-6bf12165a8df"), usp("photo-1572536147248-ac59a8abfa4b")],
    description: "Apple AirPods Pro 2nd Gen with H2 chip — up to 2× more Active Noise Cancellation, Adaptive Audio, Personalized Spatial Audio, and USB-C MagSafe charging case with up to 30 hours total battery.",
    specs: {
      Chip:                  "H2",
      Connectivity:          "Bluetooth 5.3",
      "Noise Cancellation":  "Active Noise Cancellation + Transparency Mode + Adaptive Audio",
      "Battery (Buds)":      "6 hours (ANC on), 6.5 hours (ANC off)",
      "Battery (Case)":      "30 hours total with case",
      Charging:              "USB-C, MagSafe, Qi",
      "Water Resistance":    "IP54 (buds & case)",
      Codec:                 "AAC",
      "Spatial Audio":       "Personalized with dynamic head tracking",
      "Find My":             "Precision Finding",
    },
    featured: true,
    variantAxes: [],
  },

  {
    name: "Samsung Galaxy Buds3 Pro",
    category: "Electronics",
    basePrice: 249,
    priceRange: [229, 249],
    images: [usp("photo-1590658268037-6bf12165a8df"), usp("photo-1572536147248-ac59a8abfa4b")],
    description: "Samsung Galaxy Buds3 Pro with blade-style design, 360° Audio with head tracking, AI-powered ANC, and 360° audio. Compatible with Galaxy AI for real-time interpretation.",
    specs: {
      Connectivity:       "Bluetooth 5.4",
      Codecs:             "SBC, AAC, SSC (Samsung Seamless Codec)",
      ANC:                "Intelligent ANC + Voice Detect",
      "Battery (Buds)":   "6 hours (ANC on)",
      "Battery (Total)":  "30 hours with case",
      Charging:           "USB-C, Wireless charging",
      "Water Resistance": "IP57 (buds), IPX4 (case)",
      Microphone:         "3-mic array + 2 inner mics",
      Weight:             "5.5g per bud",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["White", "Silver"] },
    ],
  },

  {
    name: "boAt Rockerz 550 Bluetooth Headphones",
    category: "Electronics",
    basePrice: 49,
    priceRange: [39, 59],
    images: [usp("photo-1505740420928-5e560c06d30e"), usp("photo-1484704849700-f032a568e944")],
    description: "boAt Rockerz 550 — over-ear Bluetooth headphones with 20-hour battery, 40mm dynamic drivers, and foldable design. Immersive ASAP charging (10 min = 2 hours music).",
    specs: {
      "Driver Size":    "40mm dynamic",
      Connectivity:     "Bluetooth 5.0, 3.5mm AUX",
      "Battery Life":   "20 hours",
      Charging:         "Micro-USB, ASAP Charge",
      "Frequency Response": "20Hz – 20kHz",
      Weight:           "220g",
      Foldable:         "Yes",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Active Black", "On Pristine White", "Jazzy Blue", "Raging Red", "Furious Green"] },
    ],
  },

  // ─── Smart TVs ──────────────────────────────────────────────────────────

  {
    name: "LG OLED evo C3 65\"",
    category: "Electronics",
    basePrice: 1799,
    priceRange: [1699, 1799],
    images: [usp("photo-1593784991095-a205069470b6"), usp("photo-1522869635100-9f4c5e86aa37")],
    description: "LG OLED evo C3 65\" — self-lit OLED pixels deliver perfect blacks, infinite contrast, and 120Hz refresh with Dolby Vision, Dolby Atmos, and NVIDIA G-SYNC. The ultimate home cinema TV.",
    specs: {
      "Screen Size":   "65 inches",
      Resolution:      "4K UHD (3840×2160)",
      Panel:           "OLED evo (self-lit pixels)",
      HDR:             "Dolby Vision IQ, HDR10, HLG",
      "Refresh Rate":  "120Hz (TruMotion 240)",
      Processor:       "α9 AI Processor Gen6",
      OS:              "webOS 23 with ThinQ AI",
      "HDMI Ports":    "4× HDMI 2.1 (48Gbps)",
      "USB Ports":     "3× USB",
      Gaming:          "NVIDIA G-SYNC, FreeSync Premium Pro, VRR, ALLM",
      Audio:           "60W, 4.2ch, Dolby Atmos, AI Sound Pro",
      Dimensions:      "144.1 × 83.0 × 4.6cm (without stand)",
    },
    featured: true,
    variantAxes: [],
  },

  {
    name: "Samsung 55\" QLED 4K Smart TV (Q80C)",
    category: "Electronics",
    basePrice: 999,
    priceRange: [899, 1099],
    images: [usp("photo-1571415060716-baff5f717de8"), usp("photo-1593784991095-a205069470b6")],
    description: "Samsung Q80C QLED — 100% Color Volume with Quantum Dot, Neo Quantum Processor 4K, and Quantum HDR 12X. Built for gaming and streaming with 4× HDMI 2.1 ports.",
    specs: {
      "Screen Size":   "55 inches",
      Resolution:      "4K UHD (3840×2160)",
      Panel:           "QLED (Quantum Dot LED)",
      HDR:             "Quantum HDR 12X, HDR10+",
      "Refresh Rate":  "120Hz",
      Processor:       "Neo Quantum Processor 4K",
      OS:              "Tizen (Samsung Smart TV)",
      "HDMI Ports":    "4× HDMI 2.1",
      Audio:           "40W, Object Tracking Sound+",
      Gaming:          "FreeSync Premium Pro, ALLM, 144Hz Game Mode",
    },
    featured: false,
    variantAxes: [
      { attr: "Screen Size", values: [
        { val: "55 inch",   priceOffset: 0   },
        { val: "65 inch",   priceOffset: 300 },
        { val: "75 inch",   priceOffset: 700 },
      ]},
    ],
  },

  // ─── Smartwatches ───────────────────────────────────────────────────────

  {
    name: "Apple Watch Series 9 GPS",
    category: "Electronics",
    basePrice: 399,
    priceRange: [399, 449],
    images: [usp("photo-1579586337278-3befd40fd17a"), usp("photo-1523275335684-37898b6baf30")],
    description: "Apple Watch Series 9 with the new S9 chip, Double Tap gesture, brighter Always-On Retina display, on-device Siri, precision finding for iPhone, and advanced health sensors.",
    specs: {
      Chip:              "Apple S9 SiP (64-bit dual-core)",
      Display:           "Always-On Retina LTPO OLED (up to 2000 nits)",
      Health:            "ECG, Blood Oxygen, Temperature, Crash Detection",
      "Heart Rate":      "Optical heart sensor, irregular rhythm notification",
      GPS:               "L1 + L5 dual-frequency",
      "Water Resistance":"50m (swim-proof)",
      Battery:           "Up to 18 hours (36hrs Low Power Mode)",
      Connectivity:      "Bluetooth 5.3, Wi-Fi 802.11b/g/n, UWB U1",
      "Double Tap":      "Yes (new gesture to interact without touching screen)",
    },
    featured: true,
    variantAxes: [
      { attr: "Case Size", values: [
        { val: "41mm", priceOffset: 0  },
        { val: "45mm", priceOffset: 30 },
      ]},
      { attr: "Band Material", values: ["Sport Band", "Sport Loop", "Braided Solo Loop"] },
    ],
  },

  {
    name: "Samsung Galaxy Watch 6 Classic",
    category: "Electronics",
    basePrice: 399,
    priceRange: [349, 399],
    images: [usp("photo-1508685096489-7aacd43bd3b1"), usp("photo-1523275335684-37898b6baf30")],
    description: "Samsung Galaxy Watch 6 Classic with rotating bezel, BioActive Sensor for comprehensive health monitoring, advanced sleep coaching, and 3-day battery life.",
    specs: {
      Display:           "Super AMOLED (47mm: 480×480, 43mm: 432×432)",
      Processor:         "Exynos W930 dual-core 1.4GHz",
      RAM:               "2GB",
      Storage:           "16GB",
      Health:            "BioActive (HR, SpO2, ECG, Skin Temp, Body Composition)",
      GPS:               "GPS/GLONASS/BeiDou/Galileo",
      "Water Resistance":"5ATM + IP68",
      Battery:           "300mAh (47mm), up to 40hrs",
      OS:                "Wear OS 4 powered by Samsung",
      Connectivity:      "Bluetooth 5.3, Wi-Fi 2.4/5GHz, NFC",
    },
    featured: false,
    variantAxes: [
      { attr: "Case Size", values: [
        { val: "43mm", priceOffset: 0  },
        { val: "47mm", priceOffset: 30 },
      ]},
      { attr: "Color", values: ["Black", "Silver"] },
    ],
  },

  {
    name: "Garmin Forerunner 255",
    category: "Electronics",
    basePrice: 349,
    priceRange: [299, 349],
    images: [usp("photo-1579586337278-3befd40fd17a"), usp("photo-1508685096489-7aacd43bd3b1")],
    description: "Garmin Forerunner 255 — advanced running GPS watch with daily suggested workouts, HRV status, sleep tracking, multi-band GPS, and 14-day battery life. Built for serious runners.",
    specs: {
      Display:           "1.3-inch MIP (260×260) always-on",
      GPS:               "GPS, GLONASS, Galileo, QZSS (multi-band optional)",
      Health:            "Heart rate, SpO2, stress, HRV Status, body battery",
      "Running Dynamics": "Cadence, stride length, ground contact time",
      Battery:           "14 days smartwatch, 30 hours GPS",
      "Water Resistance":"5ATM",
      Music:             "Up to 500 songs (Forerunner 255 Music)",
      Weight:            "49g",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Midnight", "Whitestone", "Aqua"] },
    ],
  },

  // ─── Tablets ────────────────────────────────────────────────────────────

  {
    name: "Apple iPad Pro 11\" M4",
    category: "Electronics",
    basePrice: 999,
    priceRange: [999, 999],
    images: [usp("photo-1544244015-0df4b3ffc6b0"), usp("photo-1561154464-82e9adf32764")],
    description: "iPad Pro 11\" with M4 chip — thinnest Apple product ever at 5.3mm. Ultra Retina XDR OLED tandem display, Apple Pencil Pro support, and Magic Keyboard compatibility for a true laptop replacement.",
    specs: {
      Chip:            "Apple M4, 10-core CPU, 10-core GPU",
      Display:         "11-inch Ultra Retina XDR (OLED Tandem), 2420×1668 at 264 ppi",
      RAM:             "8GB / 16GB (1TB+)",
      Cameras:         "12MP Wide + 12MP Ultra Wide (rear), 12MP landscape (front)",
      Battery:         "Up to 10 hours",
      Charging:        "USB-C (Thunderbolt 4)",
      "Apple Pencil":  "Apple Pencil Pro (hover + squeeze)",
      Connectivity:    "Wi-Fi 6E, Bluetooth 5.3, optional 5G",
      "Face ID":       "Landscape Face ID",
      Thickness:       "5.3mm",
    },
    featured: true,
    variantAxes: [
      { attr: "Storage", values: [
        { val: "256GB",  priceOffset: 0   },
        { val: "512GB",  priceOffset: 200 },
        { val: "1TB",    priceOffset: 400 },
        { val: "2TB",    priceOffset: 800 },
      ]},
      { attr: "Connectivity", values: [
        { val: "Wi-Fi",            priceOffset: 0   },
        { val: "Wi-Fi + Cellular", priceOffset: 200 },
      ]},
    ],
  },

  {
    name: "Samsung Galaxy Tab S9+",
    category: "Electronics",
    basePrice: 999,
    priceRange: [949, 999],
    images: [usp("photo-1561154464-82e9adf32764"), usp("photo-1544244015-0df4b3ffc6b0")],
    description: "Samsung Galaxy Tab S9+ with 12.4\" Dynamic AMOLED 2X display, Snapdragon 8 Gen 2, included S Pen, IP68 water resistance, and DeX mode for desktop-level productivity.",
    specs: {
      Processor:       "Snapdragon 8 Gen 2",
      RAM:             "12GB",
      Display:         "12.4-inch Dynamic AMOLED 2X, 2800×1752 at 266 ppi, 120Hz",
      Battery:         "10090 mAh, 45W charging",
      S_Pen:           "Included (Bluetooth S Pen)",
      "Water Resistance": "IP68",
      "DeX Mode":      "Yes (desktop-level multitasking)",
      Connectivity:    "Wi-Fi 6E, Bluetooth 5.3, optional 5G",
    },
    featured: false,
    variantAxes: [
      { attr: "Storage", values: [
        { val: "256GB", priceOffset: 0   },
        { val: "512GB", priceOffset: 150 },
      ]},
      { attr: "Color", values: ["Graphite", "Beige"] },
    ],
  },

  // ─── Cameras ────────────────────────────────────────────────────────────

  {
    name: "Sony Alpha ZV-E10 II",
    category: "Electronics",
    basePrice: 849,
    priceRange: [799, 849],
    images: [usp("photo-1542038784456-1ea8e935640e"), usp("photo-1452780212441-14786e989c9f")],
    description: "Sony Alpha ZV-E10 II — vlog-focused mirrorless camera with 26MP APS-C sensor, 4K 60fps, in-body image stabilization, vertical video mode, and AI subject recognition autofocus.",
    specs: {
      Sensor:          "26.1MP Exmor R BSI APS-C CMOS",
      "ISO Range":     "100-51200 (expandable to 204800)",
      "Video":         "4K 60fps (10-bit), 1080p 120fps",
      Autofocus:       "AI-based real-time tracking, Eye/Face/Body/Animal AF",
      Stabilization:   "5-axis in-body (IBIS)",
      Screen:          "3-inch fully articulating touchscreen",
      Battery:         "NP-FZ100, 570 shots per charge",
      "Lens Mount":    "Sony E-mount (APS-C & full-frame compatible)",
      Connectivity:    "USB-C, Bluetooth 5.0, Wi-Fi",
      Weight:          "377g (with battery & card)",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Black", "White"] },
    ],
  },

  {
    name: "Canon EOS R6 Mark II",
    category: "Electronics",
    basePrice: 2499,
    priceRange: [2399, 2499],
    images: [usp("photo-1502982720700-bfff97f2ecac"), usp("photo-1617467515765-5e00b0dfe31c")],
    description: "Canon EOS R6 Mark II full-frame mirrorless camera — 40fps RAW burst, Dual Pixel CMOS AF II covering 100% of frame, 6K oversampled 4K 60fps video, and 5-axis IS up to 8 stops.",
    specs: {
      Sensor:          "24.2MP Full-Frame CMOS",
      Processor:       "DIGIC X",
      "Burst Rate":    "40fps (electronic), 12fps (mechanical)",
      Video:           "6K → 4K 60fps (no crop), C-Log 3",
      Autofocus:       "Dual Pixel CMOS AF II, 1053 zones, Eye/Animal/Vehicle AF",
      Stabilization:   "5-axis IBIS up to 8 stops (Coordinated IS with RF lenses)",
      "ISO Range":     "100-102400",
      "Lens Mount":    "Canon RF",
      Battery:         "LP-E6NH, 800 shots",
      "Weather Sealing": "Yes",
      Weight:          "598g (with battery & card)",
    },
    featured: false,
    variantAxes: [],
  },

  // ─── Gaming & Peripherals ───────────────────────────────────────────────

  {
    name: "Logitech G Pro X Superlight 2",
    category: "Electronics",
    basePrice: 159,
    priceRange: [139, 159],
    images: [usp("photo-1527864550417-7fd91fc51a46"), usp("photo-1531297484001-80022131f5a1")],
    description: "Logitech G Pro X Superlight 2 — the lightest wireless gaming mouse at 60g. HERO 2 sensor with 32,000 DPI, 5 programmable buttons, 95-hour battery, and LIGHTSPEED Pro wireless.",
    specs: {
      Sensor:        "HERO 2 (32,000 DPI, 0 acceleration, 500+ IPS)",
      Weight:        "60g",
      Connectivity:  "LIGHTSPEED Pro Wireless + USB-A",
      Battery:       "95 hours",
      Buttons:       "5 programmable",
      "Report Rate": "2000Hz (POWERPLAY compatible)",
      "Zero Spin":   "True-symmetrical design",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Black", "White", "Magenta"] },
    ],
  },

  {
    name: "Razer BlackWidow V4 Pro",
    category: "Electronics",
    basePrice: 229,
    priceRange: [199, 229],
    images: [usp("photo-1511467687858-23d96c32e4ae"), usp("photo-1595225476474-87563907a212")],
    description: "Razer BlackWidow V4 Pro — wireless gaming keyboard with Razer Yellow switches, Chroma RGB per-key lighting, multi-function roller, 200-hour battery, and a dedicated media dock.",
    specs: {
      "Switch Type":    "Razer Yellow (linear, 1.2mm actuation, 45g force)",
      Layout:           "Full-size with Numpad",
      Backlighting:     "Razer Chroma RGB (per-key)",
      Connectivity:     "HyperSpeed Wireless (2.4GHz) + Bluetooth 5.0 + USB",
      Battery:          "200 hours (lighting off)",
      "Macro Keys":     "6 dedicated macro keys",
      Multimedia:       "Multi-function digital dial + 3 media keys",
      Weight:           "1230g",
    },
    featured: false,
    variantAxes: [],
  },
];

// ─────────────────────────────────────────────────────────────────────────────
// CLOTHING (22 products — Indian + international, Myntra / Amazon inspired)
// ─────────────────────────────────────────────────────────────────────────────

const CLOTHING_SIZES_MEN    = ["XS", "S", "M", "L", "XL", "XXL", "3XL"];
const CLOTHING_SIZES_WOMEN  = ["XS", "S", "M", "L", "XL", "XXL"];
const JEANS_SIZES           = ["28", "30", "32", "34", "36", "38"];
const SHOE_SIZES_UK         = ["6", "7", "8", "9", "10", "11"];

const CLOTHING = [

  // ─── Men's Tops ──────────────────────────────────────────────────────────

  {
    name: "Lacoste Classic Piqué Polo Shirt",
    category: "Clothing",
    basePrice: 99,
    priceRange: [89, 109],
    images: [usp("photo-1594938298603-c8148c4b8a6d"), usp("photo-1581655353564-df123a1eb820")],
    description: "The iconic Lacoste polo — 100% piqué cotton, embroidered crocodile, mother-of-pearl buttons, and a spread collar. A wardrobe staple since 1933.",
    specs: {
      Material:    "100% Cotton Piqué",
      Fit:         "Regular Fit",
      Collar:      "Spread collar with 2-button placket",
      Occasion:    "Casual, Smart Casual",
      Care:        "Machine wash cold, do not bleach",
      Origin:      "France",
      "Sleeve Type": "Short Sleeve",
    },
    featured: true,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_MEN },
      { attr: "Color", values: ["White", "Navy Blue", "Racing Green", "Flamingo Pink", "Electric Blue"] },
    ],
  },

  {
    name: "H&M Slim Fit Oxford Shirt",
    category: "Clothing",
    basePrice: 39,
    priceRange: [29, 49],
    images: [usp("photo-1596755094514-f87e34085b2c"), usp("photo-1602810318383-e386cc2a3ccf")],
    description: "H&M Slim Fit Oxford Shirt in soft woven cotton with a button-down collar. A versatile shirt that works dressed up with trousers or dressed down with jeans.",
    specs: {
      Material:    "100% Cotton Oxford Weave",
      Fit:         "Slim Fit",
      Collar:      "Button-down",
      Closure:     "Button through",
      Occasion:    "Formal, Business Casual",
      Care:        "Machine wash 40°C",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_MEN },
      { attr: "Color", values: ["White", "Blue", "Light Blue", "Striped Navy", "Pink"] },
    ],
  },

  {
    name: "Levi's 511 Slim Fit Jeans",
    category: "Clothing",
    basePrice: 69,
    priceRange: [59, 89],
    images: [usp("photo-1542272604-787c3835535d"), usp("photo-1541099649105-f69ad21f3246")],
    description: "Levi's 511 Slim Fit Jeans — sits below the waist, slim through the hip and thigh, tapered toward the ankle. Classic 5-pocket styling with signature Levi's leather patch.",
    specs: {
      Material:    "99% Cotton, 1% Elastane",
      Fit:         "Slim Fit",
      Rise:        "Mid-rise",
      Closure:     "Zip fly with 5-button waistband",
      Pockets:     "5-pocket styling",
      Occasion:    "Casual, Smart Casual",
      Care:        "Machine wash cold, inside out",
    },
    featured: true,
    variantAxes: [
      { attr: "Waist", values: JEANS_SIZES },
      { attr: "Wash",  values: ["Indigo Dark", "Light Wash", "Black Denim", "Grey Denim"] },
    ],
  },

  {
    name: "Nike Tech Fleece Joggers",
    category: "Clothing",
    basePrice: 89,
    priceRange: [79, 99],
    images: [usp("photo-1598300042247-d088f8ab3a91"), usp("photo-1544966503-7cc5ac882d5e")],
    description: "Nike Tech Fleece Joggers — lightweight yet warm double-knit Tech Fleece. Tapered fit, zippered pockets, and bonded seams for a clean, modern athletic look.",
    specs: {
      Material:    "56% Cotton, 44% Polyester Tech Fleece",
      Fit:         "Tapered Leg",
      Closure:     "Elastic waistband with internal drawcord",
      Pockets:     "2 side zip pockets, 1 back zip pocket",
      Occasion:    "Gym, Casual, Athleisure",
      Care:        "Machine wash cold",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_MEN },
      { attr: "Color", values: ["Black", "Dark Grey Heather", "Navy", "Olive"] },
    ],
  },

  {
    name: "Zara Structured Blazer",
    category: "Clothing",
    basePrice: 129,
    priceRange: [109, 149],
    images: [usp("photo-1593030761757-71fae45fa0e7"), usp("photo-1594938298603-c8148c4b8a6d")],
    description: "Zara Structured Blazer with peak lapels, padded shoulders, and a single-button closure. Fully lined interior with inner pockets. A modern take on classic tailoring.",
    specs: {
      Material:    "68% Polyester, 28% Viscose, 4% Elastane",
      Lining:      "100% Polyester",
      Fit:         "Regular Fit",
      Closure:     "Single Button",
      Lapels:      "Notched Peak Lapels",
      Pockets:     "2 flap pockets + 1 chest pocket",
      Occasion:    "Business, Smart Casual, Formal",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_MEN },
      { attr: "Color", values: ["Charcoal Grey", "Navy Blue", "Camel", "Black"] },
    ],
  },

  {
    name: "Manyavar Bandhgala Kurta Set",
    category: "Clothing",
    basePrice: 149,
    priceRange: [129, 199],
    images: [usp("photo-1583391733956-3750e0ff4e8b"), usp("photo-1583391733976-50c3c0dd3c26")],
    description: "Manyavar Bandhgala Kurta Set in rich Jacquard fabric with intricate woven motifs. Comes with matching churidar. Perfect for weddings, receptions, and festive occasions.",
    specs: {
      Material:    "Jacquard Silk Blend",
      Style:       "Bandhgala (Nehru Collar)",
      Fabric:      "Jacquard Woven",
      Dupatta:     "Not included",
      Lining:      "Polyester lining",
      Occasion:    "Wedding, Festive, Sangeet, Reception",
      "Wash Care":  "Dry clean only",
      Origin:      "India",
    },
    featured: true,
    variantAxes: [
      { attr: "Size",  values: ["38", "40", "42", "44", "46", "48"] },
      { attr: "Color", values: ["Royal Blue", "Deep Maroon", "Sage Green", "Champagne Gold"] },
    ],
  },

  {
    name: "Adidas Originals Trefoil Hoodie",
    category: "Clothing",
    basePrice: 75,
    priceRange: [65, 85],
    images: [usp("photo-1556821840-3a63f95609a7"), usp("photo-1591047139829-d91aecb6caea")],
    description: "adidas Originals Trefoil Hoodie — a heritage icon updated in soft cotton-blend fleece. Relaxed fit, kangaroo pocket, and the classic Trefoil logo on chest.",
    specs: {
      Material:    "70% Cotton, 30% Polyester French Terry",
      Fit:         "Regular / Relaxed",
      Hood:        "Fixed hood with flat drawcord",
      Pockets:     "Kangaroo front pocket",
      Cuffs:       "Ribbed cuffs and hem",
      Occasion:    "Casual, Streetwear, Gym",
      Care:        "Machine wash cold, tumble dry low",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_MEN },
      { attr: "Color", values: ["Black", "White", "Grey Heather", "Navy", "Burgundy"] },
    ],
  },

  {
    name: "The North Face Thermoball Jacket",
    category: "Clothing",
    basePrice: 249,
    priceRange: [229, 269],
    images: [usp("photo-1539533113208-f6df8cc8b543"), usp("photo-1591047139829-d91aecb6caea")],
    description: "The North Face Thermoball Eco Jacket — synthetic insulation in a water-resistant, packable jacket. Warm even when wet, folds into its own pocket for easy carry.",
    specs: {
      Shell:       "100% Recycled Nylon, DWR finish",
      Insulation:  "PrimaLoft ThermoBall Eco (synthetic, warm when wet)",
      Fit:         "Standard Fit",
      Pockets:     "2 zip hand pockets + 1 internal zip pocket, stuff-into-pocket",
      Hood:        "Attached, adjustable",
      Closure:     "Full-zip",
      "Water Resistance": "DWR (Durable Water Repellent)",
      Occasion:    "Hiking, Travel, Casual",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_MEN },
      { attr: "Color", values: ["TNF Black", "Summit Navy", "Burnt Olive", "Iron Red"] },
    ],
  },

  // ─── Women's Wear ─────────────────────────────────────────────────────────

  {
    name: "Fabindia Cotton Embroidered Kurti",
    category: "Clothing",
    basePrice: 59,
    priceRange: [49, 79],
    images: [usp("photo-1610030469983-98e550d6193c"), usp("photo-1594938298603-c8148c4b8a6d")],
    description: "Fabindia handcrafted cotton kurti with traditional hand-block print embroidery. A-line silhouette with 3/4 sleeves and V-neckline. Pairs beautifully with palazzos or leggings.",
    specs: {
      Material:    "100% Handloom Cotton",
      Embroidery:  "Hand-block print / Hand embroidery",
      Silhouette:  "A-line",
      Neck:        "V-neck with keyhole",
      Sleeves:     "3/4 length",
      Occasion:    "Casual, Festive, Daily wear",
      Care:        "Gentle machine wash cold, shade dry",
      Origin:      "India",
    },
    featured: true,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_WOMEN },
      { attr: "Color", values: ["Indigo Blue", "Mustard Yellow", "Rust Orange", "Sage Green", "Rose Pink"] },
    ],
  },

  {
    name: "W Women's Anarkali Kurta Set",
    category: "Clothing",
    basePrice: 79,
    priceRange: [69, 99],
    images: [usp("photo-1610030469983-98e550d6193c"), usp("photo-1583391733956-3750e0ff4e8b")],
    description: "W anarkali kurta set with flared skirt-style bottom and matching palazzo pants. Printed georgette fabric with beautiful drape — perfect for festivals and casual ethnic occasions.",
    specs: {
      Material:    "Georgette (polyester blend)",
      Style:       "Anarkali",
      Includes:    "Kurta + Palazzo",
      Neck:        "Round neck",
      Sleeves:     "Full sleeves with flare",
      Occasion:    "Ethnic, Festive, Party",
      Care:        "Dry clean or gentle hand wash",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_WOMEN },
      { attr: "Color", values: ["Teal Printed", "Maroon Printed", "Beige Floral", "Deep Blue"] },
    ],
  },

  {
    name: "Vero Moda Wrap Midi Dress",
    category: "Clothing",
    basePrice: 69,
    priceRange: [59, 79],
    images: [usp("photo-1515372039744-b8f02a3ae446"), usp("photo-1469334031218-e382a71b716b")],
    description: "Vero Moda wrap midi dress with a v-neckline, tiered skirt, and adjustable waist tie. Flowy viscose fabric with a beautiful drape — elegant day-to-night styling.",
    specs: {
      Material:    "95% Viscose, 5% Elastane",
      Length:      "Midi (knee-to-mid-calf)",
      Style:       "Wrap dress",
      Neck:        "Deep V-neck",
      Sleeves:     "Short Flutter Sleeves",
      Closure:     "Self-tie wrap",
      Occasion:    "Party, Date Night, Casual Smart",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_WOMEN },
      { attr: "Color", values: ["Floral Print", "Cobalt Blue", "Sage Green", "Rust", "Black"] },
    ],
  },

  {
    name: "Zivame High-Waist Leggings",
    category: "Clothing",
    basePrice: 29,
    priceRange: [24, 39],
    images: [usp("photo-1599447421416-3414500d18a5"), usp("photo-1609709295948-17d77cb2a69b")],
    description: "Zivame 4-way stretch high-waist leggings with a wide waistband, tummy control panel, and deep side pockets. Moisture-wicking fabric for yoga, gym, or everyday wear.",
    specs: {
      Material:    "78% Polyester, 22% Spandex",
      Compression: "Medium compression, tummy control",
      Waistband:   "Wide high-rise waistband",
      Pockets:     "2 side pockets",
      Occasion:    "Yoga, Gym, Running, Athleisure",
      Care:        "Machine wash cold, no fabric softener",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: CLOTHING_SIZES_WOMEN },
      { attr: "Color", values: ["Black", "Navy", "Charcoal", "Burgundy", "Olive"] },
    ],
  },

  {
    name: "Banarasi Silk Saree with Blouse Piece",
    category: "Clothing",
    basePrice: 149,
    priceRange: [129, 299],
    images: [usp("photo-1583391733976-50c3c0dd3c26"), usp("photo-1583391733956-3750e0ff4e8b")],
    description: "Authentic Banarasi Pure Silk Saree with intricate zari brocade work. Includes unstitched blouse piece. Woven in Varanasi — the heritage capital of Indian silk weaving.",
    specs: {
      Material:    "Pure Banarasi Silk with Zari",
      Length:      "5.5 meters saree + 0.8 meter blouse",
      Weave:       "Handloom zari brocade",
      Zari:        "Real silver zari / imitation zari",
      Blouse:      "Unstitched blouse piece included",
      Occasion:    "Wedding, Pooja, Festive, Reception",
      Care:        "Dry clean only",
      Origin:      "Varanasi, India",
    },
    featured: true,
    variantAxes: [
      { attr: "Color", values: ["Royal Red & Gold", "Teal & Silver", "Navy & Gold", "Maroon & Rose Gold", "Peacock Green & Gold"] },
    ],
  },

  // ─── Footwear ─────────────────────────────────────────────────────────────

  {
    name: "Nike Air Max 270",
    category: "Clothing",
    basePrice: 149,
    priceRange: [129, 169],
    images: [usp("photo-1542291026-7eec264c27ff"), usp("photo-1595950653106-6c9ebd614d3a")],
    description: "Nike Air Max 270 — inspired by the Air Max 180 and Air Max 93, the 270 features Nike's biggest heel Air unit yet for an all-day comfortable ride. Engineered mesh upper, foam midsole.",
    specs: {
      Upper:          "Engineered mesh + synthetic overlays",
      Midsole:        "Foam + Max Air 270 unit (largest heel Air unit)",
      Outsole:        "Rubber pods for traction",
      Closure:        "Lace-up",
      Profile:        "Low-top",
      "Drop Height":  "13mm heel-to-toe drop",
      Gender:         "Unisex (men's sizing)",
      Use:            "Lifestyle, casual",
    },
    featured: true,
    variantAxes: [
      { attr: "Size (UK)", values: SHOE_SIZES_UK },
      { attr: "Color",     values: ["Triple Black", "White/Black", "React Blue", "Volt/Black"] },
    ],
  },

  {
    name: "Adidas Ultraboost 23",
    category: "Clothing",
    basePrice: 189,
    priceRange: [169, 209],
    images: [usp("photo-1491553895911-0055eca6402d"), usp("photo-1542291026-7eec264c27ff")],
    description: "adidas Ultraboost 23 with responsive BOOST midsole, Primeknit+ upper, and Continental rubber outsole. The shoe that changed running — reborn for a new era.",
    specs: {
      Upper:       "Primeknit+ (recycled content)",
      Midsole:     "BOOST (full-length), Linear Energy Push system",
      Outsole:     "Continental Rubber (wet-grip)",
      Closure:     "Lace-up with pull tab",
      Profile:     "Low-top",
      "Stack Height": "36mm heel / 26mm forefoot",
      Weight:      "310g (UK9)",
      Use:         "Running, Training, Lifestyle",
    },
    featured: false,
    variantAxes: [
      { attr: "Size (UK)", values: SHOE_SIZES_UK },
      { attr: "Color",     values: ["Core Black", "Cloud White", "Grey Five", "Orbit Blue"] },
    ],
  },

  {
    name: "Woodland Men's Leather Oxford Shoes",
    category: "Clothing",
    basePrice: 89,
    priceRange: [79, 109],
    images: [usp("photo-1542291046-b300d5723ec3"), usp("photo-1491553895911-0055eca6402d")],
    description: "Woodland Men's Classic Oxford Shoes in full-grain leather with Goodyear welt construction, cushioned insole, and TPR outsole. Timeless formal footwear built to last years.",
    specs: {
      Upper:       "Full-grain genuine leather",
      Construction: "Goodyear Welt (resoleable)",
      Outsole:     "TPR (thermoplastic rubber) — slip resistant",
      Insole:      "Cushioned leather",
      Closure:     "Lace-up (Oxford toe cap)",
      Toe:         "Cap toe / Brogue toe",
      Heel:        "Block heel (1 inch)",
      Care:        "Polish with leather conditioner",
      Occasion:    "Formal, Business, Semi-formal",
    },
    featured: false,
    variantAxes: [
      { attr: "Size (UK)", values: SHOE_SIZES_UK },
      { attr: "Color",     values: ["Tan Brown", "Dark Brown", "Black"] },
    ],
  },

  {
    name: "Clarks Women's Block Heel Sandals",
    category: "Clothing",
    basePrice: 79,
    priceRange: [69, 99],
    images: [usp("photo-1543163521-1bf539c55dd2"), usp("photo-1542291046-b300d5723ec3")],
    description: "Clarks Juliet Dusk block-heel sandals with a wide ankle strap, padded footbed with Cushion Plus technology, and a block-heel for stability. Day-to-evening versatility.",
    specs: {
      Upper:       "Genuine leather with suede overlay",
      Footbed:     "OrthoLite® Cushion Plus insole",
      Outsole:     "TPR block heel",
      "Heel Height": "7cm block heel",
      Closure:     "Adjustable buckle ankle strap",
      Toe:         "Open toe",
      Occasion:    "Formal, Office, Party, Evening",
    },
    featured: false,
    variantAxes: [
      { attr: "Size (UK)", values: ["3", "4", "5", "6", "7", "8"] },
      { attr: "Color",     values: ["Nude", "Black", "Tan", "Wine Red"] },
    ],
  },

  {
    name: "Puma RS-X3 Puzzle Sneakers",
    category: "Clothing",
    basePrice: 109,
    priceRange: [89, 129],
    images: [usp("photo-1542291026-7eec264c27ff"), usp("photo-1595950653106-6c9ebd614d3a")],
    description: "Puma RS-X3 Puzzle sneakers with a retro running aesthetic, RS (Running System) cushioning in the heel, chunky outsole, and bold color-blocking. Unisex sizing.",
    specs: {
      Upper:       "Mesh + synthetic leather overlays",
      Midsole:     "RS foam (Running System cushioning)",
      Outsole:     "Rubber with grip pods",
      Closure:     "Lace-up",
      Profile:     "Low-top",
      Style:       "Chunky / Dad sneaker",
      Gender:      "Unisex",
    },
    featured: false,
    variantAxes: [
      { attr: "Size (UK)", values: SHOE_SIZES_UK },
      { attr: "Color",     values: ["White/Navy/Red", "Black/Gold", "Grey/Lime", "Triple White"] },
    ],
  },

  {
    name: "UCB Kids Printed T-Shirt",
    category: "Clothing",
    basePrice: 19,
    priceRange: [14, 24],
    images: [usp("photo-1515488042361-ee00e0ddd4e4"), usp("photo-1581655353564-df123a1eb820")],
    description: "United Colors of Benetton kids' printed T-shirt in soft 100% cotton jersey. Ribbed crew neck, short sleeves, and vibrant graphics — easy to wash, tough enough for everyday play.",
    specs: {
      Material:    "100% Combed Cotton Jersey",
      Neck:        "Ribbed crew neck",
      Print:       "Screen-printed graphic / embroidered logo",
      Fit:         "Regular fit",
      Care:        "Machine wash 40°C",
      Age:         "2-14 years",
    },
    featured: false,
    variantAxes: [
      { attr: "Age/Size", values: ["2-3Y", "3-4Y", "4-5Y", "5-6Y", "6-7Y", "7-8Y", "8-10Y", "10-12Y", "12-14Y"] },
      { attr: "Color",    values: ["White", "Sky Blue", "Yellow", "Red", "Olive Green"] },
    ],
  },

];

// ─────────────────────────────────────────────────────────────────────────────
// HOME (15 products — Amazon / Flipkart / IKEA India inspired)
// ─────────────────────────────────────────────────────────────────────────────

const HOME = [

  // ─── Smart Home & Appliances ─────────────────────────────────────────────

  {
    name: "Amazon Echo Dot (5th Gen)",
    category: "Home",
    basePrice: 49,
    priceRange: [44, 54],
    images: [usp("photo-1543512214-318c7553f230"), usp("photo-1518444005349-f3c2d1d4b49e")],
    description: "Amazon Echo Dot 5th Gen with improved audio, temperature sensor, and Eero Wi-Fi built-in. Alexa on every shelf — play music, control smart home, set timers, answer questions hands-free.",
    specs: {
      Speaker:         "1.73-inch front-firing speaker",
      "Voice Assistant": "Alexa built-in",
      Connectivity:    "Wi-Fi 802.11a/b/g/n/ac (2.4/5GHz), Bluetooth 5.2",
      "Built-in Sensor": "Temperature sensor",
      "Eero Wi-Fi":    "Built-in mesh Wi-Fi extender",
      Ports:           "USB-C (power only)",
      Dimensions:      "99mm diameter × 89mm tall",
      Power:           "15W adapter included",
    },
    featured: true,
    variantAxes: [
      { attr: "Color", values: ["Charcoal", "Glacier White", "Deep Sea Blue", "Lavender"] },
    ],
  },

  {
    name: "Dyson V15 Detect Cordless Vacuum",
    category: "Home",
    basePrice: 749,
    priceRange: [699, 749],
    images: [usp("photo-1558618666-fcd25c85cd64"), usp("photo-1574174456-e6fcc76cbb84")],
    description: "Dyson V15 Detect — the most powerful Dyson cordless with laser reveals microscopic dust, piezo sensor auto-adjusts suction, and HEPA filtration. Up to 60 min run time.",
    specs: {
      Suction:         "230 AW (Max mode)",
      "Filter":        "Whole-machine HEPA filtration",
      "Battery Life":  "Up to 60 min (Eco mode)",
      "Bin Volume":    "0.77L",
      Weight:          "3.1kg",
      "Laser Detect":  "Optical particle counter (microscopic dust visualization)",
      LCD:             "Runtime & filter life display",
      Attachments:     "High Torque cleaner head, Laser Slim Fluffy, Motorbar, crevice, mini soft dusting",
      Charging:        "Dock charging (4.5 hours full charge)",
    },
    featured: true,
    variantAxes: [],
  },

  {
    name: "Instant Pot Duo 7-in-1 Electric Pressure Cooker",
    category: "Home",
    basePrice: 99,
    priceRange: [89, 129],
    images: [usp("photo-1556909114-f6e7ad7d3136"), usp("photo-1585515320310-259814833e62")],
    description: "Instant Pot Duo 7-in-1 — pressure cooker, slow cooker, rice cooker, steamer, sauté pan, yogurt maker, and food warmer in one. Up to 70% faster cooking with 13 built-in smart programs.",
    specs: {
      Functions:       "Pressure Cook, Slow Cook, Rice, Sauté, Steam, Yogurt, Warm",
      "Smart Programs": "13 one-touch programs",
      Material:        "Stainless steel inner pot (food-grade 304)",
      "Safety Features": "10+ safety mechanisms",
      Power:           "1000W",
      Certifications:  "UL/ULC certified",
    },
    featured: false,
    variantAxes: [
      { attr: "Capacity", values: [
        { val: "3 Quart",  priceOffset: -20 },
        { val: "6 Quart",  priceOffset: 0   },
        { val: "8 Quart",  priceOffset: 30  },
      ]},
    ],
  },

  {
    name: "Philips Hue White & Color Ambiance Starter Kit",
    category: "Home",
    basePrice: 149,
    priceRange: [139, 179],
    images: [usp("photo-1507473885765-e6ed057f782c"), usp("photo-1534073828943-f801091bb18c")],
    description: "Philips Hue White & Color Ambiance — 16 million colors, warm to daylight white, app and voice control with Alexa/Google/Apple HomeKit. Starter kit includes bulbs + Hue Bridge.",
    specs: {
      Colors:          "16 million colors + white spectrum (2000K-6500K)",
      Connectivity:    "Zigbee (via Hue Bridge) + Bluetooth",
      Compatibility:   "Alexa, Google Home, Apple HomeKit, Samsung SmartThings",
      Brightness:      "800 lumens (A19/E26)",
      "Energy Saving": "9W = equivalent to 60W incandescent",
      App:             "Philips Hue app (iOS & Android)",
      Routines:        "Wake-up, Go-to-sleep, Geofencing, Away Mode",
    },
    featured: false,
    variantAxes: [
      { attr: "Pack Size", values: [
        { val: "Starter Kit (2 bulbs + Bridge)",  priceOffset: 0  },
        { val: "3 Bulbs + Bridge",                priceOffset: 50 },
        { val: "4 Bulbs + Bridge",                priceOffset: 80 },
      ]},
    ],
  },

  {
    name: "Prestige IRIS 750W Mixer Grinder",
    category: "Home",
    basePrice: 69,
    priceRange: [59, 89],
    images: [usp("photo-1574158622682-e40e69881006"), usp("photo-1556909114-f6e7ad7d3136")],
    description: "Prestige IRIS 750W 4-jar mixer grinder with stainless steel blades, 3 speed control + pulse, and overload protection. Ideal for Indian cooking — wet grinding, dry grinding, juicing, chutney.",
    specs: {
      Motor:           "750W (peak: 900W)",
      Jars:            "4 jars — 1.5L liquidizing, 1L dry grinding, 0.4L chutney, 0.4L juicer",
      Blades:          "Hardened stainless steel (3 sets)",
      Speeds:          "3 speed + pulse",
      Safety:          "Overload protection, jar locking system",
      Body:            "ABS plastic with stainless steel lid",
      Warranty:        "5 years on motor, 2 years on product",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["White", "Red & White", "Black & Silver"] },
    ],
  },

  {
    name: "KENT Grand Plus 9L RO+UV+UF Water Purifier",
    category: "Home",
    basePrice: 249,
    priceRange: [229, 279],
    images: [usp("photo-1585515320310-259814833e62"), usp("photo-1558618666-fcd25c85cd64")],
    description: "KENT Grand Plus multi-stage purification — RO + UV + UF + TDS controller with 9L storage tank. Patented Mineral RO Technology retains essential minerals. 20 L/hr purification rate.",
    specs: {
      Purification:    "RO + UV + UF + TDS Controller",
      "Tank Capacity": "9 liters",
      "Flow Rate":     "20 L/hr",
      "TDS Range":     "Up to 2000 ppm input",
      Membrane:        "100 GPD RO membrane",
      Certifications:  "NSF, WQA Gold Seal, ISI",
      Power:           "60W",
      Warranty:        "1 year + optional 3-year comprehensive AMC",
    },
    featured: false,
    variantAxes: [],
  },

  // ─── Bedding & Furniture ─────────────────────────────────────────────────

  {
    name: "Wakefit Orthopaedic Memory Foam Mattress",
    category: "Home",
    basePrice: 399,
    priceRange: [349, 599],
    images: [usp("photo-1631049307264-da0ec9d70304"), usp("photo-1555041469-a586c61ea9bc")],
    description: "Wakefit Orthopaedic Memory Foam Mattress with multi-layer foam construction — comfort foam on top, high-resilience support base below. 7-zone support for perfect spinal alignment. 100-night trial.",
    specs: {
      Layers:          "3-layer: Memory Foam + HR Foam + Base Foam",
      Firmness:        "Medium-firm (6/10)",
      Height:          "6 inches",
      "Cover Fabric":  "Knitted cotton stretch cover (removable, washable)",
      Certifications:  "CertiPUR-US foam",
      Trial:           "100-night free trial, easy returns",
      Warranty:        "10 years",
      "Anti-Microbial": "Anti-microbial and anti-dust-mite treated",
    },
    featured: true,
    variantAxes: [
      { attr: "Size", values: [
        { val: "Single (78×36 in)",    priceOffset: -100 },
        { val: "Double (78×48 in)",    priceOffset: -50  },
        { val: "Queen (78×60 in)",     priceOffset: 0    },
        { val: "King (78×72 in)",      priceOffset: 100  },
      ]},
    ],
  },

  {
    name: "Bombay Dyeing 300TC Egyptian Cotton Bedsheet Set",
    category: "Home",
    basePrice: 49,
    priceRange: [39, 69],
    images: [usp("photo-1631049307264-da0ec9d70304"), usp("photo-1584100936595-c0654b55a2e2")],
    description: "Bombay Dyeing premium 300 thread count 100% Egyptian cotton bedsheet set with 2 pillow covers. Silky-smooth finish, pre-shrunk, colour-fast, and machine washable.",
    specs: {
      Material:        "100% Egyptian Cotton",
      "Thread Count":  "300 TC",
      Weave:           "Sateen",
      Includes:        "1 bedsheet + 2 pillow covers",
      "Pillow Cover":  "45×70cm",
      Care:            "Machine wash cold, tumble dry low",
      "Colour Fast":   "Yes — reactive dyeing process",
    },
    featured: false,
    variantAxes: [
      { attr: "Bed Size", values: [
        { val: "Single (60×90 in)",  priceOffset: -10 },
        { val: "Double (90×100 in)", priceOffset: 0   },
        { val: "King (108×108 in)",  priceOffset: 15  },
      ]},
      { attr: "Color", values: ["White", "Ivory", "Powder Blue", "Sage Green", "Dusty Rose"] },
    ],
  },

  {
    name: "IKEA KALLAX 2x4 Shelf Unit",
    category: "Home",
    basePrice: 129,
    priceRange: [119, 179],
    images: [usp("photo-1555041469-a586c61ea9bc"), usp("photo-1493663284031-b7e3aaa4cab4")],
    description: "IKEA KALLAX 2×4 shelf unit — the versatile storage classic. Use horizontally as a TV stand or vertically as a bookcase. Compatible with KALLAX inserts, baskets, and boxes.",
    specs: {
      Dimensions:      "77×147cm (H×W), 39cm depth",
      Cells:           "8 open cells (each 33×33cm interior)",
      Material:        "Particleboard with foil finish",
      Load:            "13kg per shelf cell",
      Assembly:        "Self-assembly (tools included)",
      Inserts:         "Compatible with KALLAX door/drawer inserts",
      "Floor Protector": "Pads included to protect floors",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: [
        { val: "White",              priceOffset: 0   },
        { val: "Black-Brown",        priceOffset: 0   },
        { val: "Grey",               priceOffset: 10  },
        { val: "High-Gloss White",   priceOffset: 20  },
      ]},
    ],
  },

  {
    name: "Ergonomic Mesh Office Chair",
    category: "Home",
    basePrice: 299,
    priceRange: [249, 399],
    images: [usp("photo-1580480055273-228ff5388ef8"), usp("photo-1505797149-35ebcb05a6fd")],
    description: "High-back ergonomic mesh office chair with lumbar support, adjustable armrests, headrest, and tilt tension. Built for 8-hour workdays — breathable mesh back keeps you cool.",
    specs: {
      "Back Height":     "High-back with adjustable headrest",
      Lumbar:            "2D adjustable lumbar support",
      Armrests:          "4D adjustable (height, width, pivot, depth)",
      Seat:              "High-density foam, waterfall edge",
      "Tilt Mechanism":  "Synchro tilt with 5 lock positions",
      Base:              "5-star aluminium base with 360° swivel castors",
      "Weight Capacity": "150kg",
      Material:          "Breathable nylon mesh back, fabric seat",
      Certifications:    "BIFMA certified",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Black Mesh", "Grey Mesh", "Blue Mesh"] },
    ],
  },

  // ─── Cookware & Kitchen ──────────────────────────────────────────────────

  {
    name: "Hawkins Contura Hard Anodised Pressure Cooker",
    category: "Home",
    basePrice: 59,
    priceRange: [49, 79],
    images: [usp("photo-1585515320310-259814833e62"), usp("photo-1574158622682-e40e69881006")],
    description: "Hawkins Contura hard-anodised pressure cooker with induction-compatible base, ergonomic handles, and the iconic Hawkins safety valve. Cooks 50% faster than conventional cooking.",
    specs: {
      Material:          "Hard anodised aluminium (4mm thick base)",
      Compatibility:     "Gas + Induction + Electric cooktops",
      "Safety Features": "Safety valve, gasket-release system, pressure indicator",
      "Handles":         "Flame-proof, stay-cool Bakelite handles",
      Certifications:    "ISI marked",
      Warranty:          "5 years on body, 2 years on gasket",
    },
    featured: false,
    variantAxes: [
      { attr: "Capacity", values: [
        { val: "2 Litre",  priceOffset: -15 },
        { val: "3 Litre",  priceOffset: -5  },
        { val: "5 Litre",  priceOffset: 0   },
        { val: "7.5 Litre",priceOffset: 15  },
      ]},
    ],
  },

  {
    name: "Pigeon Non-stick Induction Cookware Set",
    category: "Home",
    basePrice: 79,
    priceRange: [69, 109],
    images: [usp("photo-1574158622682-e40e69881006"), usp("photo-1556909114-f6e7ad7d3136")],
    description: "Pigeon non-stick induction-compatible cookware set with 5-layer non-stick coating, ergonomic soft-touch handles, and glass lids. Safe for metal utensils. Dishwasher safe.",
    specs: {
      Coating:          "5-layer PTFE non-stick (PFOA-free)",
      Material:         "Heavy-gauge hard anodised aluminium body",
      Compatibility:    "Induction + Gas + Electric + Ceramic",
      Lids:             "Heat-resistant borosilicate glass with steam vent",
      Handles:          "Stainless steel with silicone grip",
      "Dishwasher Safe": "Yes",
      Certifications:   "LFGB (Germany food-grade certified)",
    },
    featured: false,
    variantAxes: [
      { attr: "Set Size", values: [
        { val: "3-Piece Set", priceOffset: -20 },
        { val: "5-Piece Set", priceOffset: 0   },
        { val: "7-Piece Set", priceOffset: 25  },
      ]},
    ],
  },

  {
    name: "Lifelong Smart LED TV 43\"",
    category: "Home",
    basePrice: 349,
    priceRange: [329, 379],
    images: [usp("photo-1593784991095-a205069470b6"), usp("photo-1522869635100-9f4c5e86aa37")],
    description: "Lifelong 43\" Full HD Smart LED TV with Android TV OS, Google Assistant, Chromecast built-in, HDR10 support, and DTS-HD sound. Slim bezel design at an unbeatable price.",
    specs: {
      "Screen Size":    "43 inches",
      Resolution:       "Full HD (1920×1080)",
      Panel:            "LED IPS",
      HDR:              "HDR10",
      OS:               "Android TV 11 with Google Play Store",
      "HDMI Ports":     "3× HDMI",
      "USB Ports":      "2× USB",
      Audio:            "24W (2×12W) with DTS-HD",
      "Google Assistant": "Built-in",
      Chromecast:       "Built-in",
    },
    featured: false,
    variantAxes: [],
  },

  {
    name: "Milton Thermosteel 24 Hour Hot & Cold Flask",
    category: "Home",
    basePrice: 25,
    priceRange: [19, 35],
    images: [usp("photo-1578946956088-940c3b502864"), usp("photo-1563213126-a4273aed2016")],
    description: "Milton Thermosteel double-wall vacuum insulated flask — keeps beverages hot for 24 hours and cold for 24 hours. Food-grade stainless steel interior, leak-proof lid.",
    specs: {
      Material:         "Food-grade 304 stainless steel inner & outer",
      Insulation:       "Double-wall vacuum insulated",
      "Hot Retention":  "24 hours",
      "Cold Retention": "24 hours",
      Lid:              "Leak-proof wide mouth screw cap",
      "BPA Free":       "Yes",
      Cleaning:         "Hand wash recommended",
    },
    featured: false,
    variantAxes: [
      { attr: "Capacity", values: [
        { val: "500ml",  priceOffset: -8  },
        { val: "750ml",  priceOffset: 0   },
        { val: "1000ml", priceOffset: 7   },
        { val: "1500ml", priceOffset: 15  },
      ]},
      { attr: "Color", values: ["Silver", "Midnight Blue", "Burgundy", "Olive Green"] },
    ],
  },

  {
    name: "Solimo Premium Microfibre Reversible Comforter",
    category: "Home",
    basePrice: 49,
    priceRange: [39, 69],
    images: [usp("photo-1584100936595-c0654b55a2e2"), usp("photo-1631049307264-da0ec9d70304")],
    description: "Amazon Solimo double-sided microfibre comforter with hypoallergenic hollow fibre fill. Reversible design with two patterns. Lightweight warmth for all seasons.",
    specs: {
      Material:         "100% Microfibre shell, polyester hollow fill",
      Fill:             "800g GSM hollow fibre (hypoallergenic, anti-bacterial)",
      Construction:     "Box stitch — prevents fill from shifting",
      Reversible:       "Yes — 2 patterns in 1",
      Care:             "Machine washable, tumble dry low",
      Certifications:   "Oeko-Tex Standard 100",
    },
    featured: false,
    variantAxes: [
      { attr: "Size", values: [
        { val: "Single (60×90 in)",  priceOffset: -10 },
        { val: "Double (90×100 in)", priceOffset: 0   },
        { val: "King (108×108 in)",  priceOffset: 15  },
      ]},
      { attr: "Color", values: ["Grey Geometric", "Navy Floral", "Ivory Solid", "Teal Abstract"] },
    ],
  },
];

// ─────────────────────────────────────────────────────────────────────────────
// BOOKS (10 products — bestsellers across genres, with format variants)
// ─────────────────────────────────────────────────────────────────────────────

const BOOKS = [

  {
    name: "Atomic Habits by James Clear",
    category: "Books",
    basePrice: 14,
    priceRange: [12, 18],
    images: [usp("photo-1512428559087-560fa5ceab42"), usp("photo-1532012197367-22857f7e831c")],
    description: "Atomic Habits — the global bestseller on building good habits and breaking bad ones. James Clear's proven framework: tiny 1% improvements compound into remarkable results. Over 15 million copies sold.",
    specs: {
      Author:    "James Clear",
      Publisher: "Penguin Random House / Avery",
      Pages:     "320",
      Language:  "English",
      Genre:     "Self-help, Productivity",
      ISBN:      "978-0735211292",
      Edition:   "First Edition (2018)",
    },
    featured: true,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",         priceOffset: 0   },
        { val: "Hardcover",         priceOffset: 12  },
        { val: "Audiobook (CD)",    priceOffset: 18  },
      ]},
    ],
  },

  {
    name: "Sapiens: A Brief History of Humankind",
    category: "Books",
    basePrice: 16,
    priceRange: [13, 22],
    images: [usp("photo-1543004629-ff569f872783"), usp("photo-1544947950-fa07a98d237f")],
    description: "Sapiens by Yuval Noah Harari — a sweeping narrative of human history from the Stone Age to the Silicon Age. Named a favourite book by Barack Obama, Bill Gates, and Mark Zuckerberg.",
    specs: {
      Author:    "Yuval Noah Harari",
      Publisher: "Harper Collins",
      Pages:     "464",
      Language:  "English",
      Genre:     "Non-fiction, History, Anthropology",
      ISBN:      "978-0062316110",
      Awards:    "Translated to 65+ languages, 25M+ copies sold",
    },
    featured: true,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",  priceOffset: 0  },
        { val: "Hardcover",  priceOffset: 14 },
      ]},
    ],
  },

  {
    name: "Clean Code by Robert C. Martin",
    category: "Books",
    basePrice: 45,
    priceRange: [40, 55],
    images: [usp("photo-1515879218367-8466d910aaa4"), usp("photo-1512428559087-560fa5ceab42")],
    description: "Clean Code — A Handbook of Agile Software Craftsmanship by Uncle Bob. The definitive guide to writing readable, maintainable code. Required reading for every software professional.",
    specs: {
      Author:    "Robert C. Martin (Uncle Bob)",
      Publisher: "Pearson / Prentice Hall",
      Pages:     "431",
      Language:  "English",
      Genre:     "Technology, Software Engineering, Programming",
      ISBN:      "978-0132350884",
      Edition:   "1st Edition (2008)",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",  priceOffset: 0  },
        { val: "Hardcover",  priceOffset: 20 },
      ]},
    ],
  },

  {
    name: "Designing Data-Intensive Applications",
    category: "Books",
    basePrice: 55,
    priceRange: [49, 65],
    images: [usp("photo-1515879218367-8466d910aaa4"), usp("photo-1543004629-ff569f872783")],
    description: "Designing Data-Intensive Applications by Martin Kleppmann — the definitive guide to distributed systems, databases, and data engineering. Essential reading for senior engineers building scalable systems.",
    specs: {
      Author:    "Martin Kleppmann",
      Publisher: "O'Reilly Media",
      Pages:     "616",
      Language:  "English",
      Genre:     "Technology, Distributed Systems, Databases",
      ISBN:      "978-1449373320",
      Edition:   "1st Edition (2017)",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",  priceOffset: 0  },
        { val: "Hardcover",  priceOffset: 15 },
      ]},
    ],
  },

  {
    name: "The Alchemist by Paulo Coelho",
    category: "Books",
    basePrice: 11,
    priceRange: [9, 15],
    images: [usp("photo-1544947950-fa07a98d237f"), usp("photo-1512428559087-560fa5ceab42")],
    description: "The Alchemist by Paulo Coelho — a magical fable about following your dreams. The most translated book by a living author with 150 million copies sold in 80 languages. A timeless classic.",
    specs: {
      Author:    "Paulo Coelho",
      Publisher: "HarperOne",
      Pages:     "208",
      Language:  "English (Translated from Portuguese)",
      Genre:     "Fiction, Philosophical Fiction, Inspirational",
      ISBN:      "978-0062315007",
      Awards:    "International bestseller — 150M+ copies sold, 80 languages",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",         priceOffset: 0   },
        { val: "Hardcover Special", priceOffset: 15  },
        { val: "Illustrated Edition", priceOffset: 20 },
      ]},
    ],
  },

  {
    name: "The Psychology of Money by Morgan Housel",
    category: "Books",
    basePrice: 13,
    priceRange: [10, 18],
    images: [usp("photo-1532012197367-22857f7e831c"), usp("photo-1556910103-1c02745aae4d")],
    description: "The Psychology of Money — 19 timeless stories about wealth, greed, and happiness by Morgan Housel. One of the most accessible and insightful books on personal finance ever written.",
    specs: {
      Author:    "Morgan Housel",
      Publisher: "Harriman House",
      Pages:     "256",
      Language:  "English",
      Genre:     "Personal Finance, Behavioural Economics, Self-help",
      ISBN:      "978-0857199096",
      Awards:    "3M+ copies sold, Amazon Business Bestseller",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",  priceOffset: 0  },
        { val: "Hardcover",  priceOffset: 12 },
      ]},
    ],
  },

  {
    name: "Harry Potter Complete 7-Book Collection",
    category: "Books",
    basePrice: 79,
    priceRange: [69, 109],
    images: [usp("photo-1551029506-0807df4e2031"), usp("photo-1512428559087-560fa5ceab42")],
    description: "The complete Harry Potter series by J.K. Rowling — all 7 books in one boxed set. Philosopher's Stone through Deathly Hallows. The greatest fantasy saga of the modern era.",
    specs: {
      Author:    "J.K. Rowling",
      Publisher: "Bloomsbury / Scholastic",
      Books:     "7 volumes",
      Language:  "English",
      Genre:     "Fantasy, Young Adult, Adventure",
      "Set Includes": "Books 1–7 in a collectible slipcase",
    },
    featured: true,
    variantAxes: [
      { attr: "Edition", values: [
        { val: "Paperback Boxed Set",           priceOffset: 0  },
        { val: "Hardcover Boxed Set",           priceOffset: 60 },
        { val: "Illustrated Edition (Vol 1-3)", priceOffset: 80 },
      ]},
    ],
  },

  {
    name: "Rich Dad Poor Dad by Robert Kiyosaki",
    category: "Books",
    basePrice: 12,
    priceRange: [9, 17],
    images: [usp("photo-1556910103-1c02745aae4d"), usp("photo-1532012197367-22857f7e831c")],
    description: "Rich Dad Poor Dad — the #1 personal finance book of all time. Robert Kiyosaki's lessons on financial literacy, investing, and building wealth that the rich teach their kids but the poor and middle class don't.",
    specs: {
      Author:    "Robert T. Kiyosaki",
      Publisher: "Warner Books / Plata Publishing",
      Pages:     "336",
      Language:  "English",
      Genre:     "Personal Finance, Investment, Self-help",
      ISBN:      "978-1612680194",
      Awards:    "32M+ copies sold worldwide",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",                    priceOffset: 0  },
        { val: "Hardcover 20th Anniversary",   priceOffset: 15 },
      ]},
    ],
  },

  {
    name: "Ikigai: The Japanese Secret to a Long Life",
    category: "Books",
    basePrice: 10,
    priceRange: [8, 14],
    images: [usp("photo-1544947950-fa07a98d237f"), usp("photo-1512428559087-560fa5ceab42")],
    description: "Ikigai by Héctor García and Francesc Miralles — the Japanese philosophy of finding your purpose in life. Based on research in Okinawa, the land of centenarians. Short, profound, and life-changing.",
    specs: {
      Author:    "Héctor García & Francesc Miralles",
      Publisher: "Penguin Books",
      Pages:     "208",
      Language:  "English (Translated from Spanish)",
      Genre:     "Self-help, Philosophy, Japanese Culture",
      ISBN:      "978-0143130727",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",  priceOffset: 0  },
        { val: "Hardcover",  priceOffset: 10 },
      ]},
    ],
  },

  {
    name: "The Great Indian Novel by Shashi Tharoor",
    category: "Books",
    basePrice: 14,
    priceRange: [11, 18],
    images: [usp("photo-1543004629-ff569f872783"), usp("photo-1451187580459-43490279c0fa")],
    description: "Shashi Tharoor's satirical masterpiece retelling the Mahabharata as modern Indian political history. A witty, incisive, and deeply original work that captures India's post-independence saga.",
    specs: {
      Author:    "Shashi Tharoor",
      Publisher: "Penguin Books India",
      Pages:     "432",
      Language:  "English",
      Genre:     "Indian Fiction, Political Satire, Historical Fiction",
      ISBN:      "978-0140110593",
      Origin:    "India",
    },
    featured: false,
    variantAxes: [
      { attr: "Format", values: [
        { val: "Paperback",  priceOffset: 0  },
        { val: "Hardcover",  priceOffset: 12 },
      ]},
    ],
  },
];

// ─────────────────────────────────────────────────────────────────────────────
// SPORTS (13 products — gym, racket sports, cricket, yoga, cycling)
// ─────────────────────────────────────────────────────────────────────────────

const SPORTS = [

  // ─── Fitness & Gym ────────────────────────────────────────────────────────

  {
    name: "Powermax Fitness TDA-125 Motorised Treadmill",
    category: "Sports",
    basePrice: 599,
    priceRange: [549, 649],
    images: [usp("photo-1571019613454-1cb2f99b2d8b"), usp("photo-1517836357463-d25dfeac3438")],
    description: "Powermax TDA-125 motorised treadmill with 3HP motor, 12 preset programs, auto incline (0-12%), foldable deck, and a 5-inch LCD display with pulse sensor. Max speed: 16 km/h.",
    specs: {
      Motor:           "3HP peak (2.25HP continuous duty)",
      Speed:           "1 – 16 km/h (0.1 increments)",
      Incline:         "Auto 0% – 12% (12 levels)",
      Belt:            "125 × 41cm running surface",
      Programs:        "12 preset + 3 custom + 1 HRC",
      Display:         "5-inch multi-window LCD",
      "Max User Weight": "110kg",
      Foldable:        "Yes (SpaceSaver fold-up design)",
      Safety:          "Emergency stop safety key",
    },
    featured: false,
    variantAxes: [],
  },

  {
    name: "Strauss Neoprene Dumbbell Set",
    category: "Sports",
    basePrice: 29,
    priceRange: [22, 89],
    images: [usp("photo-1583454110551-21f2fa20019b"), usp("photo-1517836357463-d25dfeac3438")],
    description: "Strauss neoprene-coated dumbbell set — non-slip grip, odour-resistant coating, and hexagonal ends to prevent rolling. Perfect for home workouts, toning, and rehabilitation.",
    specs: {
      Material:        "Cast iron core, neoprene coating",
      Shape:           "Hexagonal ends (no-roll design)",
      Handle:          "Knurled chrome steel",
      "Sold As":       "Pair",
      Use:             "Strength training, toning, physiotherapy",
    },
    featured: false,
    variantAxes: [
      { attr: "Weight (per pair)", values: [
        { val: "2kg × 2",   priceOffset: -18 },
        { val: "4kg × 2",   priceOffset: -10 },
        { val: "6kg × 2",   priceOffset: 0   },
        { val: "8kg × 2",   priceOffset: 15  },
        { val: "10kg × 2",  priceOffset: 30  },
        { val: "12kg × 2",  priceOffset: 50  },
      ]},
    ],
  },

  {
    name: "Decathlon Domyos 6mm Yoga Mat",
    category: "Sports",
    basePrice: 25,
    priceRange: [19, 39],
    images: [usp("photo-1544367567-0f2fcb009e0b"), usp("photo-1599447421416-3414500d18a5")],
    description: "Decathlon Domyos 6mm yoga mat with non-slip textured surface, high-density foam, and alignment lines printed on top. Includes carry strap. Ideal for yoga, pilates, and stretching.",
    specs: {
      Thickness:       "6mm",
      Material:        "TPE (Thermoplastic Elastomer) — latex-free, eco-friendly",
      Dimensions:      "183 × 61cm",
      Weight:          "1.1kg",
      Grip:            "Dual-sided texture (NBR top / TPE bottom)",
      Includes:        "Carry strap",
      "Alignment Lines": "Printed guide lines",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Purple", "Black", "Blue", "Green", "Pink", "Coral Red"] },
    ],
  },

  {
    name: "Boldfit Loop Resistance Bands Set",
    category: "Sports",
    basePrice: 19,
    priceRange: [14, 29],
    images: [usp("photo-1517836357463-d25dfeac3438"), usp("photo-1583454110551-21f2fa20019b")],
    description: "Boldfit set of 5 loop resistance bands in progressive resistance levels. Made from 100% natural latex — use for squats, glute workouts, hip abduction, physiotherapy, and stretching.",
    specs: {
      Material:        "100% Natural Latex",
      Bands:           "5 resistance levels (X-Light to X-Heavy)",
      Dimensions:      "30cm loop × 7.5cm wide",
      Use:             "Legs, glutes, physiotherapy, stretching, Pilates",
      "Storage Bag":   "Included",
    },
    featured: false,
    variantAxes: [
      { attr: "Set", values: [
        { val: "5-Band Set (XL to XH)",    priceOffset: 0  },
        { val: "3-Band Set (Light-Heavy)",  priceOffset: -8 },
      ]},
    ],
  },

  {
    name: "Cosco Leather Speed Skipping Rope",
    category: "Sports",
    basePrice: 14,
    priceRange: [11, 19],
    images: [usp("photo-1517836357463-d25dfeac3438"), usp("photo-1544367567-0f2fcb009e0b")],
    description: "Cosco genuine leather speed skipping rope with ball-bearing handle rotation for smooth, fast turns. Adjustable length for all heights. Used by professional boxers and athletes.",
    specs: {
      Cable:           "Genuine leather",
      Handles:         "Foam-grip wooden handles",
      Bearings:        "Ball-bearing rotation for speed jumping",
      Length:          "Adjustable 2.8m",
      Use:             "Cardio, boxing training, speed jump",
    },
    featured: false,
    variantAxes: [
      { attr: "Handle Color", values: ["Red", "Blue", "Black", "Green"] },
    ],
  },

  // ─── Racket Sports ────────────────────────────────────────────────────────

  {
    name: "Yonex Astrox 88S Pro Badminton Racket",
    category: "Sports",
    basePrice: 149,
    priceRange: [139, 169],
    images: [usp("photo-1611251135345-18c56206b863"), usp("photo-1558618666-fcd25c85cd64")],
    description: "Yonex Astrox 88S Pro — head-heavy balance, rotational generator system, and Isometric frame for enhanced smash power at the net. Used by world-ranked doubles players.",
    specs: {
      Frame:            "HM Graphite + Tungsten",
      Flex:             "Extra Stiff",
      Balance:          "Head-Heavy",
      "Stringing Tension": "20–28 lbs",
      Weight:           "83–87g (3U)",
      Grip:             "G5 (88mm)",
      "String Pattern": "76 holes",
      Use:              "Advanced / competitive doubles play",
    },
    featured: false,
    variantAxes: [
      { attr: "Grip Size", values: [
        { val: "G4 (86mm)",  priceOffset: 0 },
        { val: "G5 (83mm)",  priceOffset: 0 },
      ]},
    ],
  },

  {
    name: "Babolat Pure Drive 300g Tennis Racket",
    category: "Sports",
    basePrice: 229,
    priceRange: [199, 249],
    images: [usp("photo-1617083934555-ac54c7f6a19b"), usp("photo-1558618666-fcd25c85cd64")],
    description: "Babolat Pure Drive 300g tennis racket — the iconic power racket used by Carlos Alcaraz. FSI Power technology, HTR System bumper, and cortex pure feel for explosive power with control.",
    specs: {
      Weight:          "300g (strung)",
      "Head Size":     "100 sq in (645 cm²)",
      Length:          "27 inches (68.6cm)",
      Balance:         "320mm (7pts head-heavy)",
      "String Pattern": "16×19",
      Flex:            "RA 71",
      Material:        "Graphite + Basalt Fibre",
      Grip:            "L2 (4¼ inch)",
    },
    featured: false,
    variantAxes: [
      { attr: "Grip Size", values: [
        { val: "L1 (4⅛)",  priceOffset: 0 },
        { val: "L2 (4¼)",  priceOffset: 0 },
        { val: "L3 (4⅜)",  priceOffset: 0 },
      ]},
    ],
  },

  // ─── Cricket ──────────────────────────────────────────────────────────────

  {
    name: "SG Sunny Tonny Kashmir Willow Cricket Bat",
    category: "Sports",
    basePrice: 59,
    priceRange: [49, 89],
    images: [usp("photo-1531415074968-036ba1b575da"), usp("photo-1517466787929-bc90951d0974")],
    description: "SG Sunny Tonny Kashmir Willow cricket bat with a full-sized blade, extra thick edges, low sweet spot, and full cane handle with 2 rubbers. Ideal for hard tennis balls and turf wickets.",
    specs: {
      Wood:            "Grade A Kashmir Willow",
      Handle:          "Premium cane with 2 rubbers + PVC sleeve",
      Edges:           "35mm thick edges",
      "Sweet Spot":    "Low middle",
      Weight:          "1.15–1.25 kg",
      Grade:           "Grade A — no knocks required",
      Use:             "Hard ball, turf / leather ball cricket",
    },
    featured: false,
    variantAxes: [
      { attr: "Size", values: [
        { val: "SH (Short Handle)", priceOffset: 0  },
        { val: "LH (Long Handle)",  priceOffset: 10 },
      ]},
    ],
  },

  {
    name: "Nivia Storm Football (Size 5)",
    category: "Sports",
    basePrice: 29,
    priceRange: [24, 39],
    images: [usp("photo-1431324155629-1a6deb1dec8d"), usp("photo-1546519638-68e109498ffc")],
    description: "Nivia Storm machine-stitched football with 32-panel design, butyl bladder for excellent air retention, and synthetic PVC outer. Approved for recreational play on grass and synthetic turf.",
    specs: {
      Panels:          "32 (machine-stitched)",
      Outer:           "PVC synthetic leather",
      Bladder:         "Butyl (superior air retention)",
      Circumference:   "68–70cm (Size 5)",
      Weight:          "410–450g",
      Surface:         "Grass, artificial turf",
    },
    featured: false,
    variantAxes: [
      { attr: "Size", values: [
        { val: "Size 3 (kids 8-12yr)",  priceOffset: -5  },
        { val: "Size 4 (youth 12-16yr)", priceOffset: -2  },
        { val: "Size 5 (adult/match)",   priceOffset: 0   },
      ]},
    ],
  },

  // ─── Swimming & Cycling ───────────────────────────────────────────────────

  {
    name: "Speedo Endurance+ Jammer Swimsuit",
    category: "Sports",
    basePrice: 49,
    priceRange: [44, 64],
    images: [usp("photo-1560090995-7e276a02ed11"), usp("photo-1544367567-0f2fcb009e0b")],
    description: "Speedo Endurance+ Jammer with PowerFlex Eco fabric (70% recycled chlorine-resistant polyester). Compression fit, flat seam construction, and drawcord waistband for training and competition.",
    specs: {
      Material:        "70% recycled polyester, 30% Lycra® Xtra Life",
      Technology:      "Endurance+ chlorine-resistant fabric",
      Fit:             "Compression (Jammer — knee to waist)",
      "Chlorine Resistance": "20× longer than standard swimwear",
      UPF:             "UPF 50+",
      Construction:    "Flat locked seams — reduces drag",
      "Eco Credentials": "70% recycled content (GRS certified)",
    },
    featured: false,
    variantAxes: [
      { attr: "Size",  values: ["28", "30", "32", "34", "36", "38"] },
      { attr: "Color", values: ["Navy", "Black", "Red", "Blue/Grey"] },
    ],
  },

  {
    name: "Firefox Bikes Bolt 26T Mountain Bicycle",
    category: "Sports",
    basePrice: 349,
    priceRange: [299, 449],
    images: [usp("photo-1558618666-fcd25c85cd64"), usp("photo-1534787238916-9ba6764efd4f")],
    description: "Firefox Bolt 26T MTB with 21-speed Shimano SIS gearing, front fork suspension, V-brakes, and alloy double-wall rims. A rugged and capable mountain bike for trail riding and commuting.",
    specs: {
      Frame:           "Alloy 6061 MTB geometry",
      Wheels:          "26-inch alloy double-wall rims",
      Gearing:         "21-speed (Shimano SIS 3×7)",
      Suspension:      "Front fork suspension — 80mm travel",
      Brakes:          "Alloy V-brakes (front & rear)",
      Tyres:           "26 × 2.10-inch knobby MTB tyres",
      Saddle:          "Sport padded saddle",
      Weight:          "14.5kg",
    },
    featured: false,
    variantAxes: [
      { attr: "Frame Size", values: [
        { val: "17-inch (5'2\"–5'9\")",  priceOffset: 0  },
        { val: "19-inch (5'9\"–6'2\")",  priceOffset: 20 },
      ]},
      { attr: "Color", values: ["Matt Black", "Red & Black", "Blue & Grey"] },
    ],
  },

  {
    name: "Vivo Whey Protein Chocolate Fudge 2kg",
    category: "Sports",
    basePrice: 59,
    priceRange: [49, 79],
    images: [usp("photo-1517836357463-d25dfeac3438"), usp("photo-1583454110551-21f2fa20019b")],
    description: "Vivo Life Perform whey protein concentrate — 24g protein per serving, grass-fed whey, complete amino acid profile, added digestive enzymes, and no artificial flavours. Mixes instantly.",
    specs: {
      "Protein Per Serving": "24g",
      "Serving Size":  "30g",
      Servings:        "66 servings (2kg)",
      Source:          "Grass-fed whey concentrate (UK/NZ)",
      "BCAA Content":  "5.4g BCAAs per serving",
      Sweetener:       "Stevia (no sucralose, no aspartame)",
      Certifications:  "Informed Sport (batch-tested), non-GMO",
      "Allergens":     "Contains milk (whey). Gluten-free.",
    },
    featured: false,
    variantAxes: [
      { attr: "Flavor", values: [
        { val: "Chocolate Fudge",     priceOffset: 0  },
        { val: "Vanilla Matcha",      priceOffset: 0  },
        { val: "Salted Caramel",      priceOffset: 0  },
        { val: "Unflavoured",         priceOffset: -5 },
      ]},
      { attr: "Size", values: [
        { val: "1kg (33 servings)",   priceOffset: -30 },
        { val: "2kg (66 servings)",   priceOffset: 0   },
      ]},
    ],
  },

  {
    name: "Cosco Volleyball (Official Size)",
    category: "Sports",
    basePrice: 24,
    priceRange: [18, 32],
    images: [usp("photo-1546519638-68e109498ffc"), usp("photo-1431324155629-1a6deb1dec8d")],
    description: "Cosco Championship volleyball with 18-panel design, butyl bladder, and hand-stitched PVC leather outer. Approved for recreational and club-level indoor and beach volleyball.",
    specs: {
      Panels:          "18 (hand-stitched)",
      Outer:           "PVC micro-fibre leather",
      Bladder:         "Butyl (superior air retention)",
      Circumference:   "65–67cm (official size)",
      Weight:          "260–280g",
      Pressure:        "0.30–0.325 kgf/cm²",
      Surface:         "Indoor and beach",
    },
    featured: false,
    variantAxes: [
      { attr: "Color", values: ["Blue/Yellow", "White/Green", "Multi-color"] },
    ],
  },
];

// =============================================================================
// COMBINED CATALOG
// =============================================================================
const ALL_TEMPLATES = [
  ...ELECTRONICS,
  ...CLOTHING,
  ...HOME,
  ...BOOKS,
  ...SPORTS,
];

// =============================================================================
// GENERATION LOGIC
// =============================================================================

async function createProduct(token, template) {
  const headers = { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };

  // Random price within range (±5% variance on base)
  const priceVariance = template.basePrice * 0.05;
  const price = round2(
    Math.min(
      template.priceRange[1],
      Math.max(template.priceRange[0], template.basePrice + rand(-priceVariance, priceVariance))
    )
  );

  const payload = {
    name:        template.name,
    price,
    category:    template.category,
    stock:       randInt(15, 150),
    rating:      round2(rand(3.8, 5.0)),
    ratingCount: randInt(12, 2500),
    description: template.description,
    images:      template.images,
    specs:       template.specs,
    featured:    template.featured ?? false,
  };

  const res = await fetch(`${BASE_URL}/products`, {
    method: "POST",
    headers,
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`${res.status} ${text}`);
  }

  return res.json(); // returns the created product with its generated id
}

async function createVariants(token, productId, productName, template) {
  if (!template.variantAxes || template.variantAxes.length === 0) return 0;

  const headers = { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };
  const variantDefs = buildVariants(template.variantAxes);
  let created = 0;

  for (const varDef of variantDefs) {
    const attrLabel = Object.entries(varDef.attributes)
      .map(([, v]) => v)
      .join("-");
    const sku = `${skuify(productName).substring(0, 20)}-${skuify(attrLabel).substring(0, 20)}`;

    const priceOverride =
      varDef.priceOffset !== 0
        ? round2(template.basePrice + varDef.priceOffset)
        : null; // null → use parent product price

    const payload = {
      sku,
      stock:      randInt(varDef.stockRange[0], varDef.stockRange[1]),
      price:      priceOverride,
      attributes: varDef.attributes,
    };

    const res = await fetch(`${BASE_URL}/products/${productId}/variants`, {
      method: "POST",
      headers,
      body: JSON.stringify(payload),
    });

    if (res.ok) {
      created++;
    } else {
      const text = await res.text();
      console.warn(`    [WARN] Variant failed (${attrLabel}): ${res.status} ${text}`);
    }
    await sleep(20); // gentle rate-limit between variant calls
  }

  return created;
}

async function main() {
  const token = await getAdminToken();

  // Build the list: cycle through templates until we hit NUM_PRODUCTS
  const queue = [];
  while (queue.length < NUM_PRODUCTS) {
    queue.push(...ALL_TEMPLATES);
  }
  queue.length = NUM_PRODUCTS; // trim to exact target

  console.log(`Creating ${queue.length} products (${ALL_TEMPLATES.length} unique templates) ...\n`);

  let productCount = 0;
  let variantCount = 0;
  let failCount = 0;

  for (let i = 0; i < queue.length; i++) {
    const template = queue[i];
    const label    = `[${String(i + 1).padStart(3, "0")}/${queue.length}]`;

    try {
      const created = await createProduct(token, template);
      productCount++;

      const vCount = await createVariants(token, created.id, template.name, template);
      variantCount += vCount;

      const varInfo = vCount > 0 ? ` + ${vCount} variants` : "";
      console.log(`${label} ✓  ${template.name} ($${created.price}) [${template.category}]${varInfo}`);
    } catch (e) {
      failCount++;
      console.error(`${label} ✗  ${template.name} — ${e.message}`);
    }

    await sleep(DELAY_MS);
  }

  console.log(`\n${"─".repeat(60)}`);
  console.log(`Done — ${productCount} products created, ${variantCount} variants, ${failCount} errors.`);
  console.log(`${"─".repeat(60)}`);
}

await main();
