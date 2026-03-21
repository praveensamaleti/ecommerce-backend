// Node.js equivalent of generate_products.py — uses built-in fetch (Node 18+)
const BASE_URL = process.env.API_URL || "http://localhost:8080/api";
const ADMIN_EMAIL = "admin@example.com";
const ADMIN_PASSWORD = "admin123";
const NUM_PRODUCTS = parseInt(process.argv[2]) || 100;

const CATEGORIES = {
  Electronics: [
    { name: "Wireless Noise-Canceling Headphones", price_range: [150, 350], images: ["https://images.unsplash.com/photo-1505740420928-5e560c06d30e", "https://images.unsplash.com/photo-1546435770-a3e426bf472b"], specs: { Bluetooth: "5.2", "Battery Life": "30h", "Noise Cancellation": "Active" } },
    { name: "Mechanical Gaming Keyboard",          price_range: [80, 180],  images: ["https://images.unsplash.com/photo-1511467687858-23d96c32e4ae", "https://images.unsplash.com/photo-1595225476474-87563907a212"], specs: { "Switch Type": "Cherry MX Blue", RGB: "Per-key", Connectivity: "Wired" } },
    { name: "4K Ultra HD Monitor",                 price_range: [300, 600], images: ["https://images.unsplash.com/photo-1527443224154-c4a3942d3acf", "https://images.unsplash.com/photo-1586210579191-33b45e38fa2c"], specs: { Resolution: "3840x2160", "Refresh Rate": "144Hz", Panel: "IPS" } },
    { name: "Smartphone Pro Max",                  price_range: [800, 1200],images: ["https://images.unsplash.com/photo-1511707171634-5f897ff02aa9", "https://images.unsplash.com/photo-1592890288564-76628a30a657"], specs: { Storage: "256GB", RAM: "12GB", Camera: "108MP" } },
  ],
  Clothing: [
    { name: "Premium Cotton Hoodie",     price_range: [40, 90],  images: ["https://images.unsplash.com/photo-1556821840-3a63f95609a7", "https://images.unsplash.com/photo-1591047139829-d91aecb6caea"], specs: { Material: "100% Organic Cotton", Fit: "Relaxed", Gender: "Unisex" } },
    { name: "Slim Fit Denim Jeans",      price_range: [50, 120], images: ["https://images.unsplash.com/photo-1542272604-787c3835535d", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246"], specs: { Material: "Stretch Denim", Cut: "Slim Fit", Color: "Indigo" } },
    { name: "Performance Running Shoes", price_range: [70, 150], images: ["https://images.unsplash.com/photo-1542291026-7eec264c27ff", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a"], specs: { Sole: "Responsive Foam", Weight: "250g", Type: "Neutral" } },
    { name: "Classic Oxford Shirt",      price_range: [45, 85],  images: ["https://images.unsplash.com/photo-1596755094514-f87e34085b2c", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf"], specs: { Material: "Oxford Cotton", Collar: "Button-down", Fit: "Regular" } },
  ],
  Home: [
    { name: "Ergonomic Office Chair",  price_range: [150, 450], images: ["https://images.unsplash.com/photo-1505797149-35ebcb05a6fd", "https://images.unsplash.com/photo-1580480055273-228ff5388ef8"], specs: { "Lumbar Support": "Adjustable", Material: "Breathable Mesh", "Weight Capacity": "150kg" } },
    { name: "Smart Coffee Maker",      price_range: [80, 200],  images: ["https://images.unsplash.com/photo-1520970014086-2208d157c9e2", "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"], specs: { Connectivity: "Wi-Fi App Control", Capacity: "1.5L", "Brew Time": "3 mins" } },
    { name: "Velvet Throw Pillow Set", price_range: [25, 60],   images: ["https://images.unsplash.com/photo-1584100936595-c0654b55a2e2", "https://images.unsplash.com/photo-1551298370-9d3d53e40c81"], specs: { Material: "Soft Velvet", Size: "45x45cm", Filling: "Hypoallergenic" } },
    { name: "Minimalist Floor Lamp",   price_range: [60, 150],  images: ["https://images.unsplash.com/photo-1507473885765-e6ed057f782c", "https://images.unsplash.com/photo-1534073828943-f801091bb18c"], specs: { "Bulb Type": "LED Included", Height: "160cm", Material: "Matte Steel" } },
  ],
  Books: [
    { name: "The Art of Programming",        price_range: [30, 70], images: ["https://images.unsplash.com/photo-1512428559087-560fa5ceab42", "https://images.unsplash.com/photo-1532012197367-22857f7e831c"], specs: { Format: "Hardcover", Pages: "450", Language: "English" } },
    { name: "Mystery of the Silent Woods",   price_range: [15, 35], images: ["https://images.unsplash.com/photo-1543004629-ff569f872783", "https://images.unsplash.com/photo-1544947950-fa07a98d237f"], specs: { Genre: "Thriller", Format: "Paperback", Pages: "320" } },
    { name: "Cooking for Beginners",         price_range: [20, 50], images: ["https://images.unsplash.com/photo-1556910103-1c02745aae4d", "https://images.unsplash.com/photo-1506485334402-4a76ae69731d"], specs: { Recipes: "100+", Format: "Spiral Bound", Language: "English" } },
    { name: "Space Exploration History",     price_range: [40, 90], images: ["https://images.unsplash.com/photo-1451187580459-43490279c0fa", "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa"], specs: { Illustrations: "Full Color", Format: "Hardcover", Pages: "500" } },
  ],
  Sports: [
    { name: "Yoga Mat Premium",         price_range: [30, 80],   images: ["https://images.unsplash.com/photo-1544367567-0f2fcb009e0b", "https://images.unsplash.com/photo-1599447421416-3414500d18a5"], specs: { Thickness: "6mm", Material: "Eco-friendly TPE", Grip: "Non-slip" } },
    { name: "Adjustable Dumbbell Set",  price_range: [100, 300], images: ["https://images.unsplash.com/photo-1583454110551-21f2fa20019b", "https://images.unsplash.com/photo-1517836357463-d25dfeac3438"], specs: { "Weight Range": "5-25kg", Mechanism: "Dial System", Material: "Steel" } },
    { name: "Outdoor Basketball",       price_range: [20, 60],   images: ["https://images.unsplash.com/photo-1546519638-68e109498ffc", "https://images.unsplash.com/photo-1519861531473-9200262188bf"], specs: { Size: "7 (Official)", Material: "Composite Leather", Usage: "Indoor/Outdoor" } },
    { name: "Smart Fitness Tracker",    price_range: [50, 150],  images: ["https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6", "https://images.unsplash.com/photo-1557935728-e6d1eaabe558"], specs: { Battery: "14 Days", Waterproof: "5ATM", Features: "Heart Rate, GPS" } },
  ],
};

function rand(min, max) { return Math.random() * (max - min) + min; }
function randInt(min, max) { return Math.floor(rand(min, max + 1)); }
function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }

async function getAdminToken() {
  console.log(`Logging in as ${ADMIN_EMAIL} at ${BASE_URL}...`);
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
  });
  if (!res.ok) throw new Error(`Login failed: ${res.status} ${await res.text()}`);
  const data = await res.json();
  return data.token || data.accessToken;
}

async function generateProducts(token) {
  const headers = { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };
  const categoryNames = Object.keys(CATEGORIES);
  let count = 0;

  for (let i = 0; i < NUM_PRODUCTS; i++) {
    const category = pick(categoryNames);
    const template = pick(CATEGORIES[category]);
    const name = `${template.name} ${randInt(100, 999)}`;
    const price = Math.round(rand(...template.price_range) * 100) / 100;

    const product = {
      name,
      price,
      category,
      stock: randInt(10, 200),
      rating: Math.round(rand(3.5, 5.0) * 10) / 10,
      ratingCount: randInt(5, 500),
      description: `This is a premium ${name.toLowerCase()} in the ${category} category. Designed for comfort and high performance.`,
      images: template.images.map(img => `${img}?w=800&q=80`),
      specs: template.specs,
      featured: Math.random() < 0.5,
    };

    try {
      const res = await fetch(`${BASE_URL}/products`, { method: "POST", headers, body: JSON.stringify(product) });
      if (!res.ok) throw new Error(`${res.status} ${await res.text()}`);
      console.log(`[${i + 1}/${NUM_PRODUCTS}] Created: ${name} ($${price}) in ${category}`);
      count++;
    } catch (e) {
      console.error(`[${i + 1}/${NUM_PRODUCTS}] Error creating ${name}: ${e.message}`);
    }
  }
  console.log(`\nDone — ${count}/${NUM_PRODUCTS} products created.`);
}

const token = await getAdminToken();
await generateProducts(token);
