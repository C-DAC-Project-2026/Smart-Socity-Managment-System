import { Link } from "react-router-dom";

const FEATURES = [
  { icon: "🔔", title: "Notices & Announcements", desc: "Push society-wide notices to every resident and staff member instantly." },
  { icon: "🛠️", title: "Complaints, tracked end-to-end", desc: "Raise, assign, and resolve maintenance & security complaints with a full history." },
  { icon: "💳", title: "Maintenance Bills & Payments", desc: "Generate bills, accept online payments, and keep a clean payment ledger." },
  { icon: "👪", title: "Resident & Staff Directory", desc: "A verified, searchable directory scoped privately to your own society." },
  { icon: "🔐", title: "Approval-gated access", desc: "Nobody gets in without sign-off — from society onboarding down to every resident." },
  { icon: "🏢", title: "Built for many societies", desc: "One platform, unlimited societies — each one's data fully isolated from the rest." },
];

const STEPS = [
  { n: "1", title: "Register your society", desc: "Your management committee submits society details and creates the first Admin account." },
  { n: "2", title: "Get approved", desc: "A platform Super Admin reviews and activates your society — usually within a day." },
  { n: "3", title: "Invite residents & staff", desc: "Residents and staff pick your society by name and self-register." },
  { n: "4", title: "Admin approves members", desc: "Your Society Admin reviews each request before they can access anything." },
];

const STATS = [
  { value: "100%", label: "Data isolated per society" },
  { value: "24/7", label: "Online bill payments" },
  { value: "<1 day", label: "Typical approval time" },
  { value: "5 min", label: "Security-checked reset" },
];

const TESTIMONIALS = [
  { quote: "Notices used to get lost in a WhatsApp group. Now everyone sees them, and complaints actually get tracked to resolution.", name: "Priya Nair", role: "Society Admin, Greenview Residency" },
  { quote: "Onboarding new residents used to mean a spreadsheet nobody trusted. The approval flow finally keeps our directory clean.", name: "Arjun Mehta", role: "Committee Member, Lakeside Towers" },
  { quote: "Maintenance bill collection went from chasing people down to a dashboard that just tells you who's paid.", name: "Sunita Rao", role: "Treasurer, Palm Grove Society" },
];

function BrandMark({ className = "" }) {
  return (
    <Link to="/" className={`brand-mark ${className}`}>
      <span className="brand-mark-icon">SS</span>
      Smart Society
    </Link>
  );
}

export default function Landing() {
  return (
    <div className="landing">
      <header className="landing-nav">
        <BrandMark />
        <div className="landing-nav-links">
          <Link to="/login">Login</Link>
          <Link to="/register" className="btn-primary-sm">Get Started</Link>
        </div>
      </header>

      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow">✦ Society management, simplified</span>
          <h1>
            Run your society the way <span className="accent-text">modern communities</span> expect.
          </h1>
          <p>
            Notices, complaints, staff, visitors, maintenance bills and payments —
            one secure platform for your entire housing society, with every
            society's data kept completely private from every other.
          </p>
          <div className="hero-actions">
            <Link to="/register/society" className="btn-primary">Register your Society</Link>
            <Link to="/register/user" className="btn-outline">I'm a Resident / Staff</Link>
          </div>
          <p className="hero-fineprint">
            Already onboarded? <Link to="/login">Log in here</Link>.
          </p>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="hero-card">
            <div className="hero-card-row"><span>🔔</span> New notice: Water supply maintenance</div>
            <div className="hero-card-row"><span>🛠️</span> Complaint #204 marked Resolved</div>
            <div className="hero-card-row"><span>💳</span> Maintenance bill paid — Flat A-101</div>
            <div className="hero-card-row"><span>✅</span> 3 new members awaiting your approval</div>
          </div>
        </div>
      </section>

      <div className="stats-band">
        {STATS.map((s) => (
          <div key={s.label} className="stats-band-item">
            <div className="stats-band-value">{s.value}</div>
            <div className="stats-band-label">{s.label}</div>
          </div>
        ))}
      </div>

      <section className="section">
        <h2 className="section-title">Everything your committee needs</h2>
        <p className="section-subtitle">
          A single, secure workspace that replaces scattered spreadsheets, WhatsApp groups, and paper registers.
        </p>
        <div className="feature-grid">
          {FEATURES.map((f) => (
            <div key={f.title} className="feature-card">
              <div className="feature-icon">{f.icon}</div>
              <h3>{f.title}</h3>
              <p>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="section section-alt">
        <h2 className="section-title">How onboarding works</h2>
        <p className="section-subtitle">
          Every account on this platform is approved by a human before it can do anything —
          your community's data stays private and your membership stays clean.
        </p>
        <div className="steps-grid">
          {STEPS.map((s) => (
            <div key={s.n} className="step-card">
              <div className="step-num">{s.n}</div>
              <h3>{s.title}</h3>
              <p>{s.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="section">
        <h2 className="section-title">Trusted by committees like yours</h2>
        <div className="testimonial-grid">
          {TESTIMONIALS.map((t) => (
            <div key={t.name} className="testimonial-card">
              <div className="testimonial-stars">★★★★★</div>
              <p className="testimonial-quote">“{t.quote}”</p>
              <div className="testimonial-person">
                <div className="testimonial-avatar">
                  {t.name.split(" ").map((n) => n[0]).join("")}
                </div>
                <div>
                  <div className="testimonial-name">{t.name}</div>
                  <div className="testimonial-role">{t.role}</div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="cta-band">
        <h2>Ready to bring your society online?</h2>
        <div className="hero-actions" style={{ justifyContent: "center" }}>
          <Link to="/register/society" className="btn-primary">Register your Society</Link>
          <Link to="/register/user" className="btn-outline-light">Register as Resident / Staff</Link>
        </div>
      </section>

      <footer className="landing-footer">
        <span>© {new Date().getFullYear()} Smart Society Management</span>
      </footer>
    </div>
  );
}
